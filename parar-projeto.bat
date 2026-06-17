@echo off
setlocal
title ProjetoMTA - Parar
set "ROOT=%~dp0"

echo.
echo  Encerrando ProjetoMTA (portas 8080 e 5173)...
echo.

call :matar_porta 8080 "Back-end"
call :matar_porta 5173 "Front-end"

echo.
echo  Concluido.
pause
exit /b 0

:matar_porta
set "PORTA=%~1"
set "NOME=%~2"
for /f "tokens=5" %%p in ('netstat -ano ^| findstr ":%PORTA% " ^| findstr "LISTENING"') do (
    echo  [%NOME%] Encerrando processo PID %%p na porta %PORTA%...
    taskkill /PID %%p /F >nul 2>&1
)
exit /b 0
