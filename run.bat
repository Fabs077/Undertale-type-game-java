@echo off
setlocal

set PROJECT=c:\Users\f4b10\OneDrive\Desktop\ProyectoPrograII
set SRC=%PROJECT%\src\main\java
set OUT=%PROJECT%\target\classes
set GSON=%USERPROFILE%\.m2\repository\com\google\code\gson\gson\2.10.1\gson-2.10.1.jar

:: Crear carpeta de salida
if not exist "%OUT%" mkdir "%OUT%"

:: Recopilar todos los .java
dir /s /b "%SRC%\*.java" > "%PROJECT%\sources.txt"

:: Compilar
echo Compilando...
javac -encoding UTF-8 -cp "%GSON%" -d "%OUT%" @"%PROJECT%\sources.txt"
if errorlevel 1 (
    echo.
    echo ERROR: fallo en la compilacion. Revisa los mensajes anteriores.
    pause
    exit /b 1
)

:: Ejecutar
echo.
java -cp "%OUT%;%GSON%" com.rpg.engine.Main

endlocal
