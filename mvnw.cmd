@echo off
setlocal
set "SCRIPT_DIR=%~dp0"
set "MAVEN_VERSION=3.9.6"
set "MAVEN_HOME=%USERPROFILE%\.m2\wrapper\dists\apache-maven-%MAVEN_VERSION%\apache-maven-%MAVEN_VERSION%"
set "MAVEN_BIN=%MAVEN_HOME%\bin\mvn.cmd"

if not exist "%MAVEN_BIN%" (
    echo [INFO] Downloading Maven %MAVEN_VERSION% for first time use...
    powershell -NoProfile -ExecutionPolicy Bypass -Command ^
        "$ErrorActionPreference = 'Stop';" ^
        "$zipUrl = 'https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/%MAVEN_VERSION%/apache-maven-%MAVEN_VERSION%-bin.zip';" ^
        "$zipPath = [System.IO.Path]::Combine($env:TEMP, 'apache-maven-%MAVEN_VERSION%-bin.zip');" ^
        "$targetDir = [System.IO.Path]::Combine($env:USERPROFILE, '.m2\wrapper\dists\apache-maven-%MAVEN_VERSION%');" ^
        "[System.IO.Directory]::CreateDirectory($targetDir) | Out-Null;" ^
        "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12;" ^
        "Invoke-WebRequest -Uri $zipUrl -OutFile $zipPath;" ^
        "Expand-Archive -Path $zipPath -DestinationPath $targetDir -Force;" ^
        "Remove-Item -Path $zipPath -Force;"
    echo [INFO] Maven %MAVEN_VERSION% downloaded successfully!
)

call "%MAVEN_BIN%" %*
exit /b %ERRORLEVEL%
