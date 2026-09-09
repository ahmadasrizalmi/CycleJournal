@echo off
REM ============================================================
REM  CycleJournal - Build Release AAB saja (upload Play Console)
REM  Output: release\CycleJournal_v1.0.0.aab
REM ============================================================
echo.
echo Building Release AAB...
call gradlew.bat bundleRelease
if errorlevel 1 goto :err
echo.
copy /Y "app\build\outputs\bundle\release\app-release.aab" "release\CycleJournal_v1.0.0.aab" >nul
echo.
echo DONE! AAB ready in release\CycleJournal_v1.0.0.aab
pause
exit /b 0
:err
echo.
echo BUILD FAILED. See errors above.
pause
exit /b 1
