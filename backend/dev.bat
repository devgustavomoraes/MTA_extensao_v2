@echo off
setlocal
set "JAVA_HOME=C:\Users\guife\.jdks\openjdk-24.0.2+12-54"
set "MAVEN_HOME=C:\Users\guife\.m2\wrapper\dists\apache-maven-3.9.11-bin\6mqf5t809d9geo83kj4ttckcbc\apache-maven-3.9.11"
set "PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;C:\Program Files\nodejs;%PATH%"

cd /d "%~dp0"
set SPRING_PROFILES_ACTIVE=dev

echo Iniciando API Spring Boot em http://localhost:8080
echo Perfil: dev (TiDB via application-local.yml)
echo Pressione Ctrl+C para parar.
echo.

mvn spring-boot:run
