@echo off
setlocal EnableDelayedExpansion
title ProjetoMTA - Iniciar
set "ROOT=%~dp0"
set "JAVA_HOME=C:\Users\guife\.jdks\openjdk-24.0.2+12-54"
set "MAVEN_HOME=C:\Users\guife\.m2\wrapper\dists\apache-maven-3.9.11-bin\6mqf5t809d9geo83kj4ttckcbc\apache-maven-3.9.11"
set "NODE_DIR=C:\Program Files\nodejs"
set "PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%NODE_DIR%;%PATH%"

cd /d "%ROOT%"

echo.
echo  ============================================
echo   ProjetoMTA - Iniciando back-end + front-end
echo  ============================================
echo.

if not exist "%ROOT%frontend\node_modules\vite\bin\vite.js" (
    echo Instalando dependencias do front-end (primeira vez)...
    call "%ROOT%frontend\install-deps.bat"
    if errorlevel 1 exit /b 1
    echo.
)

call :porta_ocupada 8080
if errorlevel 1 (
    echo [OK] Back-end ja esta rodando na porta 8080.
    set "BACK_OK=1"
) else (
    echo [..] Abrindo back-end em nova janela...
    start "ProjetoMTA - Backend" cmd /k "cd /d "%ROOT%backend" && dev.bat"
    set "BACK_OK=0"
)

if "!BACK_OK!"=="0" (
    echo [..] Aguardando API ficar pronta...
    set "TENTATIVAS=0"
    :aguardar_api
    timeout /t 2 /nobreak >nul
    powershell -NoProfile -Command "try { exit ([int]-not ((Invoke-RestMethod -Uri 'http://localhost:8080/actuator/health' -TimeoutSec 2).status -eq 'UP')) } catch { exit 1 }" >nul 2>&1
    if errorlevel 1 (
        set /a TENTATIVAS+=1
        if !TENTATIVAS! LSS 45 (
            goto aguardar_api
        )
        echo [AVISO] API ainda nao respondeu. Confira a janela do back-end.
    ) else (
        echo [OK] API respondendo em http://localhost:8080
    )
)

call :porta_ocupada 5173
if errorlevel 1 (
    echo [OK] Front-end ja esta rodando na porta 5173.
) else (
    echo [..] Abrindo front-end em nova janela...
    start "ProjetoMTA - Frontend" cmd /k "cd /d "%ROOT%frontend" && dev.bat"
    timeout /t 3 /nobreak >nul
)

echo.
echo  ============================================
echo   Acesse: http://localhost:5173
echo   API:    http://localhost:8080/actuator/health
echo  ============================================
echo.
echo  CREDENCIAIS DE LOGIN (admin):
if exist "%ROOT%backend\src\main\resources\application-local.yml" (
    for /f "usebackq tokens=1,* delims=:" %%a in (`findstr /C:"admin-email" /C:"admin-password" "%ROOT%backend\src\main\resources\application-local.yml"`) do (
        set "LINHA=%%b"
        set "LINHA=!LINHA: =!"
        echo    %%a: !LINHA!
    )
) else (
    echo    E-mail: admin@mta.local
    echo    Senha:  admin123
    echo    ^(ou configure application-local.yml^)
)
echo.

echo  Para encerrar: feche as janelas Backend e Frontend
echo  ou execute parar-projeto.bat
echo.

start "" "http://localhost:5173"
pause
exit /b 0

:porta_ocupada
netstat -ano | findstr ":%1 " | findstr "LISTENING" >nul 2>&1
exit /b %errorlevel%
