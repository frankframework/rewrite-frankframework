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

REM Navigate to the target directory
pushd "%TARGET_DIR%" || (
    echo Error: Failed to navigate to target directory "%TARGET_DIR%".
    exit /b 1
)

REM Copy the pom.xml to the target directory
echo Copying pom.xml to target directory...
copy "%SOURCE_DIR%\rewritepom.xml" "rewritepom.xml"
if errorlevel 1 (
    echo Error: Failed to copy pom.xml to target directory.
    popd
    exit /b 1
)

REM Run the Maven rewrite:run command
echo Running rewrite:run with profile... 
if "%TARGET_PROFILE%"=="" (
    call mvn rewrite:run -f rewritepom.xml -Dmaven.test.skip=true -Dmaven.main.skip=true
) else (
    call mvn rewrite:run -f rewritepom.xml -Dmaven.test.skip=true -Dmaven.main.skip=true -P%TARGET_PROFILE%
)
if errorlevel 1 (
    echo Error: Maven command failed. Does build profile %TARGET_PROFILE% exist?
)

REM Cleanup: Delete the copied pom.xml
echo Cleaning up pom.xml...
del "%TARGET_DIR%\rewritepom.xml"
if errorlevel 1 (
    echo Warning: Failed to delete pom.xml in target directory.
    popd
    exit /b 1
)
cd %TARGET_DIR%
echo Done. Running ant in directory %CD%..
call ant

REM Return to the original directory
popd

exit /b 0.