@echo off
REM ============================================================
REM  CycleJournal - Build Release APK saja
REM  Output: release\CycleJournal_v1.0.0.apk
REM ============================================================
echo.
echo Building Release APK...
call gradlew.bat assembleRelease
if errorlevel 1 goto :err
echo.
copy /Y "app\build\outputs\apk\release\app-release.apk" "release\CycleJournal_v1.0.0.apk" >nul
echo.
echo DONE! APK ready in release\CycleJournal_v1.0.0.apk
pause
exit /b 0
:err
echo.
echo BUILD FAILED. See errors above.
pause
exit /b 1
