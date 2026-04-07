@echo off
setlocal EnableExtensions
REM Maven exige que JAVA_HOME sea correcta si esta definida; si apunta a una ruta
REM inexistente, falla aunque java este en PATH. Aqui se corrige o se autodetecta JDK 21.

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
