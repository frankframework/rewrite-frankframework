@echo off

set FRANK_RUNNER_DIR=..\frank-runner\

if not exist "%FRANK_RUNNER_DIR%" (
    echo "Frank-runner directory not found: %FRANK_RUNNER_DIR%"
    exit /b 1
)

set MVN_BAT=%FRANK_RUNNER_DIR%\mvn.bat

if not exist "%MVN_BAT%" (
    echo "mvn.bat not found in frank-runner directory."
    exit /b 1
)

set profiles=default 7_4 7_5 7_6 7_7 7_8 7_9 8_0 8_1 8_2 8_3 9_0
for %%p in (%profiles%) do (
    if "%%p"=="default" (
        echo Building with default profile...
        call %MVN_BAT% clean package install -U -DargLine="-XX:+EnableDynamicAgentLoading"
    ) else (
        echo Building with profile: %%p
        call %MVN_BAT% clean package install -U -P%%p -DargLine="-XX:+EnableDynamicAgentLoading"
    )
)

pause