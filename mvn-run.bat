@echo off
REM ============================================================
REM  mvn-run.bat — Run via Maven (auto-sets JAVA_HOME)
REM  Use this if 'mvn javafx:run' fails with JAVA_HOME error
REM ============================================================
echo ============================================================
echo   Hotel Management System - Maven Build and Run
echo ============================================================
echo.

REM Set JAVA_HOME to the JDK 23 installation on this machine
set "JAVA_HOME=C:\Program Files\Java\jdk-23"

echo Using JAVA_HOME: %JAVA_HOME%
echo.
echo Running: mvn javafx:run
echo.

mvn javafx:run

pause
