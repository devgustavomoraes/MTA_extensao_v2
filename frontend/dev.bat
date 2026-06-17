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

echo Iniciando Vite em http://localhost:5173
echo IMPORTANTE: suba o back-end ANTES (backend\dev.bat) e aguarde "Banco conectado".
echo Pressione Ctrl+C para parar.
echo.

"%NODE_DIR%\node.exe" "node_modules\vite\bin\vite.js" --host localhost --port 5173
