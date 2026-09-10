@echo off
REM ============================================================
REM  CycleJournal - Build APK & AAB (Release) sekaligus
REM  Output: release\CycleJournal_v1.1.2.apk & .aab
REM ============================================================
echo.
echo [1/2] Building Release APK...
call gradlew.bat assembleRelease
if errorlevel 1 goto :err
echo [2/2] Building Release AAB...
call gradlew.bat bundleRelease
if errorlevel 1 goto :err
echo.
echo Copying artifacts to release\...
copy /Y "app\build\outputs\apk\release\app-release.apk" "release\CycleJournal_v1.1.2.apk" >nul
copy /Y "app\build\outputs\bundle\release\app-release.aab" "release\CycleJournal_v1.1.2.aab" >nul
echo.
echo DONE! Files ready in release\CycleJournal_v1.1.2.apk and .aab
pause
exit /b 0
:err
echo.
echo BUILD FAILED. See errors above.
pause
exit /b 1
