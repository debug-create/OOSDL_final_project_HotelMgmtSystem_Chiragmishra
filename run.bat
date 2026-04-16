@echo off
echo ============================================
echo   Hotel Management System - Build and Run
echo ============================================
echo.

echo [1/3] Compiling Java sources...
javac --module-path lib --add-modules javafx.controls,javafx.fxml -d out src\*.java

if %errorlevel% neq 0 (
    echo.
    echo COMPILATION FAILED - Check errors above.
    pause
    exit /b 1
)
echo       Done.
echo.

echo [2/3] Copying FXML resources to output folder...
copy src\login.fxml out\ >nul 2>&1
echo       Done.
echo.

echo [3/3] Launching Hotel Management System...
java --module-path lib --add-modules javafx.controls,javafx.fxml -Djava.library.path=lib -cp out MainApp

pause