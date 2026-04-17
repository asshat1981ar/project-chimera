#!/usr/bin/env python3
"""
Security Scanner for Android Projects.

A Python tool that scans Android project files for common security vulnerabilities,
potentially dangerous patterns, and configuration issues.

Usage:
    python security_scanner.py [--path PATH] [--severity {low,medium,high,critical}]
                              [--json] [--list-rules]

Example:
    python -m src.staged_agents.security_scanner --path . --severity high
"""

from __future__ import annotations

import argparse
import json
import os
import re
import sys
from dataclasses import dataclass, field
from enum import Enum
from pathlib import Path
from typing import List, Optional


class Severity(Enum):
    """Vulnerability severity levels."""
    LOW = "low"
    MEDIUM = "medium"
    HIGH = "high"
    CRITICAL = "critical"


@dataclass
class Finding:
    """Represents a single security finding."""
    file_path: str
    line_number: Optional[int]
    severity: Severity
    rule_id: str
    message: str
    suggestion: str
    context: Optional[str] = None


@dataclass
class ScanResult:
    """Container for all scan findings."""
    findings: List[Finding] = field(default_factory=list)

    @property
    def critical_count(self) -> int:
        return sum(1 for f in self.findings if f.severity == Severity.CRITICAL)

    @property
    def high_count(self) -> int:
        return sum(1 for f in self.findings if f.severity == Severity.HIGH)

    @property
    def medium_count(self) -> int:
        return sum(1 for f in self.findings if f.severity == Severity.MEDIUM)

    @property
    def low_count(self) -> int:
        return sum(1 for f in self.findings if f.severity == Severity.LOW)

    def to_dict(self) -> dict:
        return {
            "summary": {
                "total": len(self.findings),
                "critical": self.critical_count,
                "high": self.high_count,
                "medium": self.medium_count,
                "low": self.low_count,
            },
            "findings": [
                {
                    "file_path": f.file_path,
                    "line_number": f.line_number,
                    "severity": f.severity.value,
                    "rule_id": f.rule_id,
                    "message": f.message,
                    "suggestion": f.suggestion,
                    "context": f.context,
                }
                for f in self.findings
            ],
        }


class SecurityScanner:
    """Scans Android project files for security vulnerabilities."""

    def __init__(self, root_path: str, min_severity: Severity = Severity.LOW) -> None:
        self.root_path = Path(root_path).resolve()
        self.min_severity = min_severity
        self.result = ScanResult()

        # Define security rules
        self.rules = [
            # Hardcoded secrets / credentials
            {
                "id": "HC-001",
                "name": "Hardcoded API Key",
                "severity": Severity.CRITICAL,
                "patterns": [
                    (r'(?i)(api[_-]?key|apikey|api_key)\s*[=:]\s*["\'][^"\']{20,}["\']',
                     "Hardcoded API key detected",
                     "Move API keys to secure configuration or use Android Keystore/Credentials plugin"),
                    (r'(?i)(secret[_-]?key|secretkey|secret_key)\s*[=:]\s*["\'][^"\']{20,}["\']',
                     "Hardcoded secret key detected",
                     "Move secret keys to secure configuration or environment variables"),
                    (r'(?i)(password|passwd|pwd)\s*[=:]\s*["\'][^"\']{8,}["\']',
                     "Hardcoded password detected",
                     "Use Android Keystore or secure credential storage"),
                    (r'(?i)(token|auth[_-]?token|access[_-]?token)\s*[=:]\s*["\'][^"\']{20,}["\']',
                     "Hardcoded authentication token detected",
                     "Use OAuth token storage or secure backend authentication"),
                    (r'(?i)(private[_-]?key|privatekey|private_key)\s*[=:]\s*["\'][^"\']{20,}["\']',
                     "Hardcoded private key detected",
                     "Never commit private keys to source control"),
                ],
            },
            # Debug/development flags in production
            {
                "id": "DC-001",
                "name": "Debug Mode Enabled in Production",
                "severity": Severity.HIGH,
                "patterns": [
                    (r'(?i)(debuggable|debug\s*:\s*true|application\.debug)',
                     "Debug mode may be enabled",
                     "Ensure debug mode is disabled in release builds (build.gradle)"),
                    (r'(?i)log\.\w+\(.*\)',
                     "Potential debug logging detected",
                     "Remove or guard debug logs in production code"),
                    (r'(?i)(android\.util\.Log\.v|Log\.d|Log\.v)',
                     "Verbose/debug logging detected",
                     "Strip verbose logging in release builds"),
                ],
            },
            # Insecure configurations
            {
                "id": "IC-001",
                "name": "Insecure Network Configuration",
                "severity": Severity.CRITICAL,
                "patterns": [
                    (r'(?i)android:usesCleartextTraffic\s*=\s*true',
                     "Cleartext (HTTP) traffic allowed",
                     "Set android:usesCleartextTraffic=false or use HTTPS"),
                    (r'(?i)http://[^\s"\'>]+',
                     "Insecure HTTP URL detected",
                     "Use HTTPS for all network communications"),
                    (r'(?i)(allowbackup|allowBackup)\s*[=:]\s*true',
                     "Backup feature enabled",
                     "Set android:allowBackup=false for sensitive applications"),
                ],
            },
            # Dangerous AndroidManifest permissions
            {
                "id": "MP-001",
                "name": "Dangerous Permissions",
                "severity": Severity.HIGH,
                "patterns": [
                    (r'(?i)READ_SMS', "Dangerous SMS permission", "Review necessity of SMS permission"),
                    (r'(?i)SEND_SMS', "Dangerous SMS sending permission", "Review necessity of SMS sending permission"),
                    (r'(?i)READ_CONTACTS', "Dangerous contacts reading permission", "Review necessity of contacts permission"),
                    (r'(?i)WRITE_CONTACTS', "Dangerous contacts writing permission", "Review necessity of contacts permission"),
                    (r'(?i)ACCESS_FINE_LOCATION', "Precise location permission", "Review necessity and usage of location"),
                    (r'(?i)CAMERA', "Camera permission", "Review necessity of camera permission"),
                    (r'(?i)RECORD_AUDIO', "Microphone recording permission", "Review necessity of audio recording permission"),
                ],
            },
            # Build.gradle insecure dependencies
            {
                "id": "BD-001",
                "name": "Potentially Insecure Dependencies",
                "severity": Severity.MEDIUM,
                "patterns": [
                    (r'(?i)(implementation|api|compile)\s+["\']([^"\']+)(debug|test)["\']',
                     "Debug/test dependency in build configuration",
                     "Remove debug/test dependencies from release builds"),
                    (r'(?i)(log4j|commons-collections|jackson)[=:].*1\.\d*\.\d*',
                     "Potentially vulnerable library version",
                     "Update to latest secure versions of dependencies"),
                ],
            },
            # Unused or suspicious code
            {
                "id": "UC-001",
                "name": "Suspicious Code Patterns",
                "severity": Severity.MEDIUM,
                "patterns": [
                    (r'(?i)eval\s*\(', "Use of eval() function", "Remove eval usage - potential code injection risk"),
                    (r'(?i)exec\s*\(.*shell', "Shell command execution", "Avoid shell command execution with user input"),
                    (r'(?i)Runtime\.getRuntime\(\)\.exec',
                     "Runtime command execution",
                     "Validate and sanitize all inputs before execution"),
                    (r'(?i)DES|RC4|MD5|SHA1',
                     "Weak cryptographic algorithm",
                     "Use strong algorithms like AES, SHA-256 or better"),
                ],
            },
        ]

    def _should_scan_file(self, path: Path) -> bool:
        """Determine if a file should be scanned."""
        if not path.is_file():
            return False
        if path.name.startswith("."):
            return False
        if path.name.endswith(".class"):
            return False
        if path.name.endswith(".jar"):
            return False
        return True

    def _should_scan_directory(self, path: Path) -> bool:
        """Determine if a directory should be scanned."""
        if not path.is_dir():
            return False
        excluded = {".git", ".gradle", "build", "out", "target", "__pycache__", ".pytest_cache"}
        return path.name not in excluded

    def _scan_file(self, file_path: Path) -> List[Finding]:
        """Scan a single file for security issues."""
        findings = []
        try:
            with open(file_path, "r", encoding="utf-8", errors="ignore") as f:
                lines = f.readlines()
        except (UnicodeDecodeError, OSError):
            return findings

        for line_no, line in enumerate(lines, 1):
            for rule in self.rules:
                for pattern, message, suggestion in rule["patterns"]:
                    if re.search(pattern, line):
                        if rule["severity"].value >= self.min_severity.value:
                            findings.append(Finding(
                                file_path=str(file_path.relative_to(self.root_path)),
                                line_number=line_no,
                                severity=rule["severity"],
                                rule_id=rule["id"],
                                message=message,
                                suggestion=suggestion,
                                context=line.strip()[:200],
                            ))
        return findings

    def _scan_gradle(self) -> None:
        """Scan build.gradle.kts and related Gradle files."""
        gradle_files = list(self.root_path.rglob("build.gradle*"))
        for gradle_file in gradle_files:
            self.result.findings.extend(self._scan_file(gradle_file))

    def _scan_manifest(self) -> None:
        """Scan AndroidManifest.xml files."""
        manifest_files = list(self.root_path.rglob("AndroidManifest.xml"))
        for manifest_file in manifest_files:
            self.result.findings.extend(self._scan_file(manifest_file))

    def _scan_source_files(self) -> None:
        """Scan Java/Kotlin source files."""
        source_dirs = list(self.root_path.rglob("src"))
        for src_dir in source_dirs:
            for java_file in src_dir.rglob("*.java"):
                if self._should_scan_file(java_file):
                    self.result.findings.extend(self._scan_file(java_file))
            for kotlin_file in src_dir.rglob("*.kt"):
                if self._should_scan_file(kotlin_file):
                    self.result.findings.extend(self._scan_file(kotlin_file))

    def _scan_resource_files(self) -> None:
        """Scan resource files (strings.xml, etc.)."""
        res_dirs = list(self.root_path.rglob("res"))
        for res_dir in res_dirs:
            for res_file in res_dir.rglob("*.xml"):
                if self._should_scan_file(res_file):
                    self.result.findings.extend(self._scan_file(res_file))

    def scan(self) -> ScanResult:
        """Run the full security scan."""
        if not self.root_path.exists():
            print(f"Error: Path does not exist: {self.root_path}", file=sys.stderr)
            return self.result

        # Scan all relevant files
        self._scan_gradle()
        self._scan_manifest()
        self._scan_source_files()
        self._scan_resource_files()

        # Filter by minimum severity
        self.result.findings = [
            f for f in self.result.findings
            if f.severity.value >= self.min_severity.value
        ]

        return self.result


def format_text_output(result: ScanResult) -> str:
    """Format scan results as human-readable text."""
    lines = []
    lines.append("=" * 70)
    lines.append("Security Scan Report")
    lines.append("=" * 70)
    lines.append("")

    # Summary
    lines.append("Summary:")
    lines.append(f"  Total findings: {result.total}")
    lines.append(f"  Critical: {result.critical_count}")
    lines.append(f"  High:     {result.high_count}")
    lines.append(f"  Medium:   {result.medium_count}")
    lines.append(f"  Low:      {result.low_count}")
    lines.append("")

    if not result.findings:
        lines.append("No security issues found!")
        return "\n".join(lines)

    # Group by severity
    by_severity = {s: [] for s in Severity}
    for finding in result.findings:
        by_severity[finding.severity].append(finding)

    for severity in [Severity.CRITICAL, Severity.HIGH, Severity.MEDIUM, Severity.LOW]:
        findings = by_severity[severity]
        if not findings:
            continue
        lines.append(f"\n{severity.value.upper()} SEVERITY ({len(findings)}):")
        lines.append("-" * 70)
        for i, finding in enumerate(findings, 1):
            lines.append(f"  [{i}] {finding.rule_id}")
            lines.append(f"      File: {finding.file_path}")
            if finding.line_number:
                lines.append(f"      Line: {finding.line_number}")
            lines.append(f"      Message: {finding.message}")
            if finding.context:
                lines.append(f"      Context: {finding.context}")
            lines.append(f"      Fix: {finding.suggestion}")
            lines.append("")

    return "\n".join(lines)


def main() -> None:
    """CLI entry point."""
    parser = argparse.ArgumentParser(
        description="Security scanner for Android projects",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""Examples:
  %(prog)s --path .
  %(prog)s --path . --severity high
  %(prog)s --path . --json --severity medium
        """,
    )
    parser.add_argument(
        "--path",
        default=".",
        help="Root path of the project to scan (default: current directory)",
    )
    parser.add_argument(
        "--severity",
        choices=[s.value for s in Severity],
        default="low",
        help="Minimum severity to report (default: low)",
    )
    parser.add_argument(
        "--json",
        action="store_true",
        help="Output results in JSON format",
    )
    parser.add_argument(
        "--list-rules",
        action="store_true",
        help="List all security rules and exit",
    )

    args = parser.parse_args()

    scanner = SecurityScanner(args.path, Severity(args.severity))

    if args.list_rules:
        print("Security Rules:")
        print("=" * 70)
        for rule in scanner.rules:
            print(f"\n[{rule['id']}] {rule['name']} (Severity: {rule['severity'].value})")
            for pattern, message, suggestion in rule["patterns"]:
                print(f"  - Pattern: {pattern[:60]}...")
                print(f"    Message: {message}")
                print(f"    Fix: {suggestion}")
        return

    result = scanner.scan()

    if args.json:
        print(json.dumps(result.to_dict(), indent=2))
    else:
        print(format_text_output(result))

    # Exit with error code if critical or high findings exist
    if result.critical_count > 0 or result.high_count > 0:
        sys.exit(1)


if __name__ == "__main__":
    main()