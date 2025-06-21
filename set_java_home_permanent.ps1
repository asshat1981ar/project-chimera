c:# PowerShell script to set JAVA_HOME permanently in system environment variables
# Usage: Run this script as Administrator, providing the path to your JDK installation as argument
# Example: .\set_java_home_permanent.ps1 -JdkPath "C:\Program Files\Java\jdk-17.0.2"

param(
    [Parameter(Mandatory=$true)]
    [string]$JdkPath
)

if (-Not (Test-Path $JdkPath)) {
    Write-Error "The specified JDK path does not exist: $JdkPath"
    exit 1
}

try {
    [Environment]::SetEnvironmentVariable("JAVA_HOME", $JdkPath, [EnvironmentVariableTarget]::Machine)
    $currentPath = [Environment]::GetEnvironmentVariable("Path", [EnvironmentVariableTarget]::Machine)
    if ($currentPath -notlike "*%JAVA_HOME%\\bin*") {
        $newPath = "$currentPath;%JAVA_HOME%\bin"
        [Environment]::SetEnvironmentVariable("Path", $newPath, [EnvironmentVariableTarget]::Machine)
    }
    Write-Host "JAVA_HOME set permanently to $JdkPath"
    Write-Host "Please restart your terminal or system for changes to take effect."
} catch {
    Write-Error "Failed to set environment variables. Please run this script as Administrator."
    exit 1
}
