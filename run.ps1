$javafxLib = "C:\Javafx\javafx-sdk-21.0.10\lib"

Write-Host "Compiling Bank Management System..." -ForegroundColor Cyan

if (!(Test-Path "bin")) { New-Item -ItemType Directory -Force -Path "bin" | Out-Null }

$javaFiles = Get-ChildItem -Path "src\main\java" -Filter "*.java" -Recurse | Select-Object -ExpandProperty FullName

# Redirect stderr so PowerShell does not treat javac Notes/Warnings as errors
$ErrorActionPreference = "Continue"
& javac -d bin --module-path $javafxLib @javaFiles 2>&1 | Write-Host
$compileExit = $LASTEXITCODE
$ErrorActionPreference = "Stop"

if ($compileExit -ne 0) {
    Write-Error "Compilation failed! Exit code: $compileExit"
    exit 1
}

# Copy the UI resources (like CSS files)
if (Test-Path "src\main\resources") {
    Copy-Item -Path "src\main\resources\*" -Destination bin -Recurse -Force -ErrorAction SilentlyContinue
}

Write-Host "Starting JavaFX Application..." -ForegroundColor Green
java --module-path "$javafxLib;bin" -m com.bank/com.bank.MainApp
