@echo off
setlocal enabledelayedexpansion
echo Building Game of Life executable...

REM Try to auto-detect Java installation if JAVA_HOME is not set
if "%JAVA_HOME%" == "" (
    echo JAVA_HOME not found. Attempting to auto-detect Java installation...

    REM Check common Java installation paths
    set "JAVA_PATHS=C:\Program Files\Java\ C:\Program Files\Eclipse Adoptium\ C:\Program Files\Eclipse Foundation\ C:\Program Files\Zulu\ C:\Java\"

    REM Flag to track if we found Java
    set "JAVA_FOUND=0"

    REM First try to find JDK installations
    for %%p in (%JAVA_PATHS%) do (
        if exist "%%p" (
            echo Checking directory: %%p
            for /d %%j in ("%%p\jdk*" "%%p\jre*" "%%p\java*" "%%p\openjdk*") do (
                if exist "%%j\bin\java.exe" (
                    set "JAVA_HOME=%%j"
                    echo Found Java installation at: !JAVA_HOME!
                    set "JAVA_FOUND=1"
                    goto :java_detection_done
                )
            )
        )
    )

    REM If we haven't found Java yet, try to find it directly in Program Files
    if "!JAVA_FOUND!"=="0" (
        for /d %%j in ("C:\Program Files\Java\*") do (
            if exist "%%j\bin\java.exe" (
                set "JAVA_HOME=%%j"
                echo Found Java installation at: !JAVA_HOME!
                set "JAVA_FOUND=1"
                goto :java_detection_done
            )
        )
    )

    :java_detection_done
    if "!JAVA_FOUND!"=="0" (
        echo Error: JAVA_HOME not found in your environment.
        echo Please set the JAVA_HOME variable in your environment to match the
        echo location of your Java installation.
        echo.
        echo Example: set JAVA_HOME=C:\Program Files\Java\jdk-22
        echo.
        echo You can set it temporarily for this session by running:
        echo set JAVA_HOME=path\to\your\java\installation
        echo.
        echo Common Java installation paths:
        echo - C:\Program Files\Java\jdk-*
        echo - C:\Program Files\Eclipse Adoptium\jdk-*
        echo - C:\Program Files\Eclipse Foundation\jdk-*
        echo - C:\Program Files\Zulu\jdk-*
        echo.
        echo Build failed.
        pause
        exit /b 1
    )
)

call mvnw.cmd clean package
echo.
if exist target\GameOfLife.exe (
    echo Build successful! Executable created at target\GameOfLife.exe

    REM Create src\bin directory if it doesn't exist
    if not exist src\bin mkdir src\bin

    REM Copy the executable to src\bin
    echo Copying executable to src\bin...
    copy /Y target\GameOfLife.exe src\bin\GameOfLife.exe
    if errorlevel 1 (
        echo Failed to copy executable to src\bin. Please check permissions and try again.
    ) else (
        echo Successfully copied executable to src\bin.
    )

    echo.
    echo Executable copied to src\bin\GameOfLife.exe
    echo.
    echo Vous pouvez trouver l'exécutable à l'emplacement suivant:
    echo %CD%\src\bin\GameOfLife.exe
    echo.
    echo Pour lancer l'application, double-cliquez sur le fichier GameOfLife.exe dans le dossier src\bin
) else (
    echo Build failed. Please check the error messages above.
)
pause
