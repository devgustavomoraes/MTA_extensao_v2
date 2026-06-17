@echo off
setlocal
set "NODE_DIR=C:\Program Files\nodejs"
set "PATH=%NODE_DIR%;%PATH%"

cd /d "%~dp0"

if not exist "dist\index.html" (
    echo Pasta dist\ nao encontrada. Execute build.bat primeiro.
    pause
    exit /b 1
)

echo Preview em http://localhost:4173
"%NODE_DIR%\node.exe" "node_modules\vite\bin\vite.js" preview --host 127.0.0.1 --port 4173
