@echo off
setlocal
set "VERSION=9.5.0"
set "SHA256=553c78f50dafcd54d65b9a444649057857469edf836431389695608536d6b746"
if defined GRADLE_USER_HOME (set "ROOT=%GRADLE_USER_HOME%") else (set "ROOT=%USERPROFILE%\.gradle")
set "BASE=%ROOT%\wrapper\dists\codes-gradle-%VERSION%"
set "ZIP=%BASE%\gradle-%VERSION%-bin.zip"
set "HOME_DIR=%BASE%\gradle-%VERSION%"
set "URL=https://services.gradle.org/distributions/gradle-%VERSION%-bin.zip"
if not exist "%HOME_DIR%\bin\gradle.bat" (
  if not exist "%BASE%" mkdir "%BASE%"
  powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; $tmp='%ZIP%.tmp'; Invoke-WebRequest -UseBasicParsing -Uri '%URL%' -OutFile $tmp; $actual=(Get-FileHash -Algorithm SHA256 $tmp).Hash.ToLowerInvariant(); if ($actual -ne '%SHA256%') { Remove-Item -Force $tmp; throw 'Gradle distribution checksum mismatch' }; if (Test-Path '%HOME_DIR%') { Remove-Item -Recurse -Force '%HOME_DIR%' }; Expand-Archive -Path $tmp -DestinationPath '%BASE%' -Force; Move-Item -Force $tmp '%ZIP%'"
  if errorlevel 1 exit /b %errorlevel%
)
call "%HOME_DIR%\bin\gradle.bat" %*
exit /b %errorlevel%
