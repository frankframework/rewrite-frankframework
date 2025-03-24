@echo off

REM Check if the argument is provided
if "%~dp1"=="" (
    echo Error: No target directory provided.
    echo Usage: script.bat [target_directory]
    exit /b 1
)

REM Store the current directory (where pom.xml is assumed to be located)
set "SOURCE_DIR=%cd%"
REM Store the target directory from the argument
set "TARGET_DIR=%~dp1"

REM Store target maven profile from argument
set "TARGET_PROFILE=%2"
set "SOURCE_VERSION=%3"

REM Define the valid version list
set "VALID_VERSIONS=7_3 7_4 7_5 7_6 7_7 7_8 7_9 8_0 8_1 8_2 8_3 9_0"

REM Ensure the target directory exists
if not exist "%TARGET_DIR%" (
    echo Error: Target directory "%TARGET_DIR%" does not exist.
    exit /b 1
)

REM Check if pom.xml exists in the current directory
if not exist "%SOURCE_DIR%\rewritepom.xml" (
    echo Error: rewritepom.xml not found in the current directory %SOURCE_DIR%.
    exit /b 1
)

REM Copy the pom.xml to the target directory
echo Copying pom.xml to target directory...
copy "%SOURCE_DIR%\rewritepom.xml" "%TARGET_DIR%\rewritepom.xml"
if errorlevel 1 (
    echo Error: Failed to copy pom.xml to target directory.
    exit /b 1
)

REM Define the frank runner directory and mvn.bat location
set "FRANK_RUNNER_DIR=..\..\frank-runner\"
set "MVN_BAT=%FRANK_RUNNER_DIR%\mvn.bat"

REM Check if mvn.bat exists
if not exist "%MVN_BAT%" (
    echo "mvn.bat not found in frank-runner directory."
    exit /b 1
)

REM Run the Maven rewrite:run command
echo Running rewrite:run with profile...
if "%TARGET_PROFILE%"=="" (
    REM No target arg found, running default profile
    call "%MVN_BAT%" rewrite:run -f "%TARGET_DIR%\rewritepom.xml" -Dmaven.test.skip=true -Dmaven.main.skip=true
) else if "%SOURCE_VERSION%"=="" (
    REM No source arg found, but found target profile
    call "%MVN_BAT%" rewrite:run -f "%TARGET_DIR%\rewritepom.xml" -Dmaven.test.skip=true -Dmaven.main.skip=true -P%TARGET_PROFILE%
) else (
    REM Both the target and source arg found
    setlocal enabledelayedexpansion

    set SHOULD_DO=false
    echo Trying to run recipes in range: %SOURCE_VERSION% to %TARGET_PROFILE%

    set SOURCE_FOUND=false
    set TARGET_FOUND=false

    REM Check if args are referring to valid profiles
    for %%V in (%VALID_VERSIONS%) do (
        if "%%V"=="%TARGET_PROFILE%" (
            set TARGET_FOUND=true
        )
        if "%%V"=="%SOURCE_VERSION%" (
            set SOURCE_FOUND=true
        )
    )

    REM Exit on invalid args with debug message
    if !TARGET_FOUND!==false (
        echo Target, %TARGET_PROFILE%, not found or reached, either target profile doesn't exists in rewritepom.xml or in version range: %VALID_VERSIONS%
        exit /b 1
    )
    if !SOURCE_FOUND!==false (
        echo Source version, %SOURCE_VERSION%, not supported, make sure the source version is in version range: %VALID_VERSIONS%
        exit /b 1
    )

    REM Run recipes for specified profile range
    for %%V in (%VALID_VERSIONS%) do (
        if !SHOULD_DO!==true (
            echo Running recipes from profile: %%V
            call "%MVN_BAT%" rewrite:run -f "%TARGET_DIR%\rewritepom.xml" -Dmaven.test.skip=true -Dmaven.main.skip=true -P%%V
        )

        REM When target profile is reached stop executing the rewrite plugin
        if "%%V"=="%TARGET_PROFILE%" (
            echo Reached target version: %%V. Stopping.
            set SHOULD_DO=false
        )

        REM When source version is reached start executing the rewrite plugin
        REM At the end of the for loop to prevent the previous profile from executing
        if !SHOULD_DO!==false (
            if /i "%%V"=="%SOURCE_VERSION%" (
                echo Found matching source version: %%V
                set SHOULD_DO=true
            )
        )
    )
    endlocal
)

if errorlevel 1 (
    echo Error: Maven command failed. Does build profile %TARGET_PROFILE% exist?
)

REM Cleanup: Delete the copied pom.xml
echo Cleaning up pom.xml...
del "%TARGET_DIR%\rewritepom.xml"
if errorlevel 1 (
    echo Warning: Failed to delete pom.xml in target directory.
    exit /b 1
)

echo Done. Running ant in directory %TARGET_DIR%..
cd /d "%TARGET_DIR%"
call ant
cd /d "%SOURCE_DIR%"

exit /b 0