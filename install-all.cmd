@echo off
setlocal

set "APP_ROOT=%~dp0"
for %%I in ("%APP_ROOT%..") do set "WORKSPACE_ROOT=%%~fI"

pushd "%WORKSPACE_ROOT%\jmaster-web" || exit /b 1
cmd /d /c "mvnw.cmd clean install %*"
if errorlevel 1 (
    set "EXIT_CODE=%errorlevel%"
    popd
    exit /b %EXIT_CODE%
)
popd

pushd "%APP_ROOT%" || exit /b 1
cmd /d /c "mvnw.cmd clean install %*"
if errorlevel 1 (
    set "EXIT_CODE=%errorlevel%"
    popd
    exit /b %EXIT_CODE%
)
popd

endlocal
