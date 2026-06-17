@echo off
setlocal
set "NODE_DIR=C:\Program Files\nodejs"
set "PATH=%NODE_DIR%;%PATH%"

cd /d "%~dp0"

echo Instalando dependencias do front-end...
"%NODE_DIR%\npm.cmd" install
if errorlevel 1 (
    echo.
    echo ERRO: npm install falhou.
    echo Verifique se Node.js esta em: %NODE_DIR%
    pause
    exit /b 1
)

echo.
echo OK! Dependencias instaladas.
echo Proximo passo: execute dev.bat
pause
