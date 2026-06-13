@echo off
setlocal
set "NODE_DIR=C:\Program Files\nodejs"
set "PATH=%NODE_DIR%;%PATH%"

cd /d "%~dp0"

if not exist "node_modules\vite\bin\vite.js" (
    echo Dependencias nao encontradas. Execute install-deps.bat primeiro.
    pause
    exit /b 1
)

echo Gerando build de producao em dist\ ...
"%NODE_DIR%\node.exe" "node_modules\vite\bin\vite.js" build
if errorlevel 1 pause & exit /b 1

echo.
echo Build concluido. Abra preview.bat para testar localmente.
pause
