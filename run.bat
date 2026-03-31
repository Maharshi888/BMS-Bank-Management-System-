@echo off
echo Compiling and running Bank Management System...

set JAVAFX_LIB=C:\Javafx\javafx-sdk-21.0.10\lib

if not exist "bin" mkdir "bin"

:: Get all java files to compile
dir /s /b src\main\java\*.java > sources.txt

:: Compile the java files
javac -d bin --module-path "%JAVAFX_LIB%" @sources.txt
if %errorlevel% neq 0 (
    echo Compilation failed!
    pause
    exit /b %errorlevel%
)

:: Copy resources like CSS files
if exist src\main\resources (
    xcopy src\main\resources\* bin\ /s /e /y /q >nul
)

:: Run the application
echo Starting application...
java --module-path "%JAVAFX_LIB%;bin" -m com.bank/com.bank.MainApp
