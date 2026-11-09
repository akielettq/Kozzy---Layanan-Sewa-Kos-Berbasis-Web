@echo off
echo ========================================================
echo   Menjalankan Kozzy Desktop GUI (PBO Workstation)
echo ========================================================
if exist "lib\javafx.controls.jar" (
    java --module-path "lib;C:\javafx-sdk\lib" --add-modules javafx.controls,javafx.graphics,javafx.swing --enable-native-access=ALL-UNNAMED -cp "bin;lib/*" com.kozzy.Main
) else (
    java -cp "bin;lib/*" com.kozzy.Main
)
if %ERRORLEVEL% NEQ 0 (
    echo [GALAT] Terjadi masalah saat menjalankan program.
)
pause
