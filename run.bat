@echo off
setlocal

set PROJECT=c:\Users\f4b10\OneDrive\Desktop\ProyectoPrograII
cd /d "%PROJECT%"

if "%1"=="console" (
    echo Ejecutando ConsoleTests del motor...
    call mvn -q compile exec:java -Dexec.mainClass=com.rpg.engine.ConsoleTests
) else (
    echo Compilando y ejecutando UI ^(DesktopLauncher^)...
    call mvn compile exec:java
)

endlocal
