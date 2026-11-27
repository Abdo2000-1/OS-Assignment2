@echo off
setlocal EnableExtensions

set "MAVEN_VERSION=3.9.9"
set "TOOLS_DIR=%~dp0.tools"
set "MAVEN_HOME=%TOOLS_DIR%\apache-maven-%MAVEN_VERSION%"
set "MAVEN_ZIP=%TEMP%\apache-maven-%MAVEN_VERSION%-cpu-scheduler.zip"

if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
    echo Maven was not found in this project. Downloading Maven %MAVEN_VERSION%...
    powershell.exe -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; $url='https://archive.apache.org/dist/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.zip'; $zip=$env:MAVEN_ZIP; $tools=$env:TOOLS_DIR; New-Item -ItemType Directory -Force -Path $tools | Out-Null; Invoke-WebRequest -UseBasicParsing -Uri $url -OutFile $zip; Expand-Archive -Force -Path $zip -DestinationPath $tools; Remove-Item -LiteralPath $zip -Force"
    if errorlevel 1 (
        echo Failed to download Maven. Install Maven or check your internet connection.
        exit /b 1
    )
)

rem Java/Maven can fail when the project path contains Arabic characters.
rem Use the first available temporary ASCII drive letter while Maven runs.
set "MAVEN_DRIVE="
for %%D in (Z Y X W V U T S R Q P O N M L K J I H G F E D C B A) do (
    if not defined MAVEN_DRIVE (
        subst %%D: "%~dp0" >nul 2>&1
        if not errorlevel 1 set "MAVEN_DRIVE=%%D:"
    )
)
if not defined MAVEN_DRIVE (
    echo Could not create a temporary drive for Maven. All drive letters are busy.
    exit /b 1
)

pushd %MAVEN_DRIVE%\
call "%MAVEN_DRIVE%\.tools\apache-maven-%MAVEN_VERSION%\bin\mvn.cmd" %*
set "MAVEN_EXIT_CODE=%ERRORLEVEL%"
popd
subst %MAVEN_DRIVE% /d >nul
exit /b %MAVEN_EXIT_CODE%
