@echo off
setlocal EnableExtensions
REM Maven exige que JAVA_HOME sea correcta si esta definida; si apunta a una ruta
REM inexistente, falla aunque java este en PATH. Aqui se corrige o se autodetecta JDK 21.

REM Solo PostgreSQL (Neon): al levantar la app hace falta application-local.yml junto al pom.xml.
echo %* | findstr /i /c:"spring-boot:run" /c:"spring-boot:start" >nul 2>&1
if errorlevel 1 goto :java_home
if exist "%~dp0application-local.yml" goto :java_home
echo.
echo [ERROR] Falta application-local.yml en: %~dp0
echo Esta app no usa base en memoria; solo se conecta a Neon.
echo Copia la plantilla y pon tu host, usuario y contrasena de Neon:
echo   copy application-local.yml.example application-local.yml
echo Luego edita application-local.yml ^(URL r2dbc:postgresql://...^).
echo.
exit /b 1

:java_home
if not "%JAVA_HOME%"=="" (
  if exist "%JAVA_HOME%\bin\java.exe" goto :run
  set "JAVA_HOME="
)

for /f "delims=" %%i in ('dir /b /ad "C:\Program Files\Eclipse Adoptium\jdk-21*" 2^>nul') do (
  set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\%%i"
  goto :run
)
for /f "delims=" %%i in ('dir /b /ad "C:\Program Files\Java\jdk-21*" 2^>nul') do (
  set "JAVA_HOME=C:\Program Files\Java\%%i"
  goto :run
)
for /f "delims=" %%i in ('dir /b /ad "C:\Program Files\Microsoft\jdk-21*" 2^>nul') do (
  set "JAVA_HOME=C:\Program Files\Microsoft\%%i"
  goto :run
)

echo No se encontro JDK 21. Instala Eclipse Temurin 21 ^(winget: EclipseAdoptium.Temurin.21.JDK^)
echo o define JAVA_HOME a la carpeta del JDK ^(sin \bin al final^).
exit /b 1

:run
set "PATH=%JAVA_HOME%\bin;%PATH%"
call "%~dp0mvnw.cmd" %*
set "EXIT=%ERRORLEVEL%"
endlocal & exit /b %EXIT%
