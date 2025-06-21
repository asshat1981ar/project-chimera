# PowerShell script to set JAVA_HOME and update PATH environment variable for current session
# Usage: Run this script in PowerShell, providing the path to your JDK installation as argument
# Example: .\set_java_env.ps1 -JdkPath "C:\Program Files\Java\jdk-17.0.2"

param(
    [Parameter(Mandatory=$true)]
    [string]$JdkPath
)

if (-Not (Test-Path $JdkPath)) {
    Write-Error "The specified JDK path does not exist: $JdkPath"
    exit 1
}

$env:JAVA_HOME = $JdkPath
$env:Path = "$JdkPath\bin;" + $env:Path

Write-Host "JAVA_HOME set to $env:JAVA_HOME"
Write-Host "Updated PATH environment variable for current session."

Write-Host "You can verify by running:"
Write-Host "  java -version"
Write-Host "  javac -version"
