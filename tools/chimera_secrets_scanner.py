#!/usr/bin/env python3
"""
Chimera Secrets Scanner

A security scanning tool to detect hardcoded secrets and sensitive configuration
in source code files. This helps identify potential credential leakage risks
as addressed in Issue 2: Hardcoded Secrets and Configuration in Source Code.

Usage:
    python chimera_secrets_scanner.py [--path PATH] [--severity LEVEL]
    python chimera_secrets_scanner.py --help

The tool scans common file types for patterns that indicate hardcoded secrets,
such as API keys, tokens, passwords, and endpoint URLs.
"""

import re
import sys
from dataclasses import dataclass, field
from enum import Enum
from pathlib import Path
from typing import Dict, List, Optional, Pattern, Set, Tuple


class SeverityLevel(Enum):
    """Severity levels for detected secrets."""
    CRITICAL = "CRITICAL"
    HIGH = "HIGH"
    MEDIUM = "MEDIUM"
    LOW = "LOW"


@dataclass
class SecretFinding:
    """Represents a detected secret or sensitive pattern."""
    file_path: str
    line_number: int
    line_content: str
    secret_type: str
    severity: SeverityLevel
    pattern: str = ""
    message: str = ""


@dataclass
class ScanResult:
    """Result of a secrets scan."""
    findings: List[SecretFinding] = field(default_factory=list)
    files_scanned: int = 0
    files_with_secrets: int = 0
    summary: Dict[SeverityLevel, int] = field(
        default_factory=lambda: {level: 0 for level in SeverityLevel}
    )

    def add_finding(self, finding: SecretFinding) -> None:
        """Add a finding to the scan result."""
        self.findings.append(finding)
        self.summary[finding.severity] += 1
        if finding.file_path not in [f.file_path for f in self.findings]:
            self.files_with_secrets += 1
        self.files_scanned += 1


class SecretsScanner:
    """
    Scanner for detecting hardcoded secrets in source code.
    
    This class provides methods to scan files for various types of sensitive
    information including API keys, tokens, passwords, and URLs.
    """
    
    # Common secret patterns with their severity levels
    SECRET_PATTERNS: Dict[SeverityLevel, List[Tuple[Pattern[str], str, str]]] = {
        SeverityLevel.CRITICAL: [
            (
                re.compile(r'AI_API_KEY["\']?\s*[:=]\s*["\']([^"\']+)["\']', re.IGNORECASE),
                'AI API Key',
                'High-risk AI service credentials'
            ),
            (
                re.compile(r'OPENROUTER_API_KEY["\']?\s*[:=]\s*["\']([^"\']+)["\']', re.IGNORECASE),
                'OpenRouter API Key',
                'AI service API key'
            ),
            (
                re.compile(r'api[_-]?key["\']?\s*[:=]\s*["\']([^"\']{20,})["\']', re.IGNORECASE),
                'API Key',
                'Generic API key (potential credential)'
            ),
        ],
        SeverityLevel.HIGH: [
            (
                re.compile(r'OPENAI_API_KEY["\']?\s*[:=]\s*["\']([^"\']+)["\']', re.IGNORECASE),
                'OpenAI API Key',
                'OpenAI service credentials'
            ),
            (
                re.compile(r'AWS_ACCESS_KEY_ID["\']?\s*[:=]\s*["\']([^"\']+)["\']', re.IGNORECASE),
                'AWS Access Key',
                'AWS cloud credentials'
            ),
            (
                re.compile(r'AWS_SECRET_ACCESS_KEY["\']?\s*[:=]\s*["\']([^"\']+)["\']', re.IGNORECASE),
                'AWS Secret Key',
                'AWS cloud secret credentials'
            ),
            (
                re.compile(r'STRIPE_SECRET_KEY["\']?\s*[:=]\s*["\']([^"\']+)["\']', re.IGNORECASE),
                'Stripe Secret Key',
                'Payment processing credentials'
            ),
            (
                re.compile(r'secret[_-]?key["\']?\s*[:=]\s*["\']([^"\']{20,})["\']', re.IGNORECASE),
                'Secret Key',
                'Generic secret key'
            ),
        ],
        SeverityLevel.MEDIUM: [
            (
                re.compile(r'PASSWORD["\']?\s*[:=]\s*["\']([^"\']+)["\']', re.IGNORECASE),
                'Password',
                'Plaintext password'
            ),
            (
                re.compile(r'DATABASE_URL["\']?\s*[:=]\s*["\']([^"\']+)["\']', re.IGNORECASE),
                'Database URL',
                'Database connection string'
            ),
            (
                re.compile(r'PRIVATE_KEY["\']?\s*[:=]\s*["\']([^"\']+)["\']', re.IGNORECASE),
                'Private Key',
                'Private cryptographic key'
            ),
            (
                re.compile(r'token["\']?\s*[:=]\s*["\']([^"\']{20,})["\']', re.IGNORECASE),
                'Token',
                'Authentication token'
            ),
        ],
        SeverityLevel.LOW: [
            (
                re.compile(r'ENDPOINT["\']?\s*[:=]\s*["\']([^"\']+)["\']', re.IGNORECASE),
                'Endpoint URL',
                'Service endpoint configuration'
            ),
            (
                re.compile(r'URL["\']?\s*[:=]\s*["\']([^"\']+)["\']', re.IGNORECASE),
                'URL',
                'Generic URL configuration'
            ),
        ],
    }
    
    # Files and directories to exclude from scanning
    EXCLUDE_PATTERNS: Set[str] = {
        '.git',
        '__pycache__',
        '.gradle',
        '.idea',
        '.DS_Store',
        '.svn',
        '.hg',
    }
    
    # Common file extensions to scan
    SOURCE_EXTENSIONS: Set[str] = {
        '.java',
        '.kt',
        '.kts',
        '.py',
        '.js',
        '.ts',
        '.jsx',
        '.tsx',
        '.xml',
        '.yaml',
        '.yml',
        '.json',
        '.properties',
        '.env',
        '.cfg',
        '.conf',
        '.ini',
    }
    
    def scan_directory(self, path: Path, recursive: bool = True) -> ScanResult:
        """
        Scan a directory for secrets in all supported files.
        
        Args:
            path: Directory path to scan
            recursive: Whether to scan subdirectories recursively
            
        Returns:
            ScanResult containing all findings
        """
        result = ScanResult()
        
        if not path.exists():
            print(f"Error: Path '{path}' does not exist")
            return result
        
        if path.is_file():
            if self._should_scan_file(path):
                result.add_finding(self.scan_file(path))
        else:
            for file_path in self._iter_files(path, recursive):
                if self._should_scan_file(file_path):
                    result.add_finding(self.scan_file(file_path))
        
        return result
    
    def scan_file(self, file_path: Path) -> SecretFinding:
        """
        Scan a single file for secrets.
        
        Args:
            file_path: Path to the file to scan
            
        Returns:
            SecretFinding for the first detected secret, or None if clean
        """
        try:
            with open(file_path, 'r', encoding='utf-8', errors='ignore') as f:
                for line_num, line in enumerate(f, 1):
                    for severity, patterns, _ in self.SECRET_PATTERNS.values():
                        for pattern, secret_type, message in patterns:
                            match = pattern.search(line)
                            if match:
                                return SecretFinding(
                                    file_path=str(file_path),
                                    line_number=line_num,
                                    line_content=line.strip(),
                                    secret_type=secret_type,
                                    severity=severity,
                                    pattern=pattern.pattern,
                                    message=message
                                )
        except Exception as e:
            print(f"Warning: Could not scan {file_path}: {e}")
        
        return SecretFinding(
            file_path=str(file_path),
            line_number=0,
            line_content="",
            secret_type="CLEAN",
            severity=SeverityLevel.LOW,
            message="No secrets detected"
        )
    
    def _should_scan_file(self, file_path: Path) -> bool:
        """Check if a file should be scanned based on its extension."""
        return any(file_path.suffix.lower() == ext for ext in self.SOURCE_EXTENSIONS)
    
    def _iter_files(self, path: Path, recursive: bool) -> List[Path]:
        """Iterate over files in path, excluding ignored patterns."""
        files = []
        
        if recursive:
            for item in path.rglob('*'):
                if self._should_include(item):
                    files.append(item)
        else:
            for item in path.iterdir():
                if self._should_include(item):
                    files.append(item)
        
        return files
    
    def _should_include(self, path: Path) -> bool:
        """Check if a path should be included in scanning."""
        if path.name.startswith('.') and path.name != '.env':
            return False
        
        if any(exclude in path.parts for exclude in self.EXCLUDE_PATTERNS):
            return False
        
        return True


def format_report(result: ScanResult, show_all: bool = False) -> str:
    """
    Format scan results as a human-readable report.
    
    Args:
        result: Scan result to format
        show_all: If True, show all files including clean ones
        
    Returns:
        Formatted report string
    """
    report_lines = []
    report_lines.append("=" * 70)
    report_lines.append("CHIMERA SECRETS SCAN REPORT")
    report_lines.append("=" * 70)
    report_lines.append("")
    
    # Summary
    report_lines.append("SUMMARY")
    report_lines.append("-" * 40)
    report_lines.append(f"Files scanned: {result.files_scanned}")
    report_lines.append(f"Files with secrets: {result.files_with_secrets}")
    report_lines.append("")
    
    # Per-severity summary
    report_lines.append("FINDINGS BY SEVERITY")
    report_lines.append("-" * 40)
    for severity in SeverityLevel:
        count = result.summary[severity]
        if count > 0 or show_all:
            icon = "🔴" if severity == SeverityLevel.CRITICAL else \
                   "🟠" if severity == SeverityLevel.HIGH else \
                   "🟡" if severity == SeverityLevel.MEDIUM else \
                   "🟢"
            report_lines.append(f"  {icon} {severity.value}: {count}")
    report_lines.append("")
    
    # Detailed findings
    if result.findings:
        report_lines.append("DETAILED FINDINGS")
        report_lines.append("-" * 40)
        
        for i, finding in enumerate(result.findings, 1):
            if not show_all and finding.severity == SeverityLevel.LOW:
                continue
            
            report_lines.append(f"")
            report_lines.append(f"Finding #{i}:")
            report_lines.append(f"  File: {finding.file_path}")
            report_lines.append(f"  Line: {finding.line_number}")
            report_lines.append(f"  Severity: {finding.severity.value}")
            report_lines.append(f"  Type: {finding.secret_type}")
            report_lines.append(f"  Message: {finding.message}")
            
            if finding.line_content:
                # Truncate long lines
                content = finding.line_content
                if len(content) > 100:
                    content = content[:100] + "..."
                report_lines.append(f"  Content: {content}")
    else:
        report_lines.append("✅ No secrets detected!")
    
    report_lines.append("")
    report_lines.append("=" * 70)
    
    return "\n".join(report_lines)


def main():
    """Main entry point for the secrets scanner."""
    import argparse
    
    parser = argparse.ArgumentParser(
        description="Scan Chimera project for hardcoded secrets and sensitive configuration"
    )
    parser.add_argument(
        "--path",
        type=str,
        default=".",
        help="Path to scan for secrets (default: current directory)"
    )
    parser.add_argument(
        "--severity",
        type=str,
        choices=[s.value for s in SeverityLevel],
        default=None,
        help="Minimum severity level to report (default: all)"
    )
    parser.add_argument(
        "--show-clean",
        action="store_true",
        help="Show files that were scanned but found no secrets"
    )
    parser.add_argument(
        "--min-severity",
        type=str,
        choices=[s.value for s in SeverityLevel],
        help="Minimum severity threshold for reporting findings"
    )
    parser.add_argument(
        "--output",
        type=str,
        choices=["text", "json"],
        default="text",
        help="Output format (default: text)"
    )
    parser.add_argument(
        "--help",
        action="help",
        help="Show this help message and exit"
    )
    
    args = parser.parse_args()
    
    # Determine minimum severity threshold
    min_severity = SeverityLevel.CRITICAL
    if args.severity:
        min_severity = SeverityLevel(args.severity)
    elif args.min_severity:
        min_severity = SeverityLevel(args.min_severity)
    
    # Create scanner and run scan
    scanner = SecretsScanner()
    path = Path(args.path).resolve()
    
    print(f"Scanning '{path}' for secrets...")
    result = scanner.scan_directory(path)
    
    # Filter findings by severity
    filtered_findings = [
        f for f in result.findings 
        if SeverityLevel(f.severity) >= min_severity
    ]
    result.findings = filtered_findings
    
    # Generate and output report
    report = format_report(result, show_all=args.show_clean)
    
    if args.output == "json":
        import json
        findings_data = []
        for finding in result.findings:
            findings_data.append({
                "file": finding.file_path,
                "line": finding.line_number,
                "severity": finding.severity.value,
                "type": finding.secret_type,
                "message": finding.message,
                "content": finding.line_content
            })
        output = json.dumps({
            "summary": {
                "files_scanned": result.files_scanned,
                "files_with_secrets": result.files_with_secrets,
                "total_findings": len(result.findings)
            },
            "findings": findings_data
        }, indent=2)
        print(output)
    else:
        print(report)
    
    # Exit with error code if critical findings found
    if result.summary[SeverityLevel.CRITICAL] > 0:
        return 1
    elif result.summary[SeverityLevel.HIGH] > 0:
        return 1
    
    return 0


if __name__ == "__main__":
    sys.exit(main())