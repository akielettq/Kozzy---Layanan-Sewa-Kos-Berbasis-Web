@echo off
echo ========================================================
echo   Mengompilasi Kozzy Desktop GUI (PBO Workstation)
echo ========================================================
if not exist "bin" mkdir "bin"

dir /s /b src\*.java > sources.txt
javac -encoding UTF-8 -cp "lib/*;bin" -d bin @sources.txt

if %ERRORLEVEL% EQU 0 (
    echo [SUKSES] Kompilasi berhasil tanpa galat!
) else (
    echo [GALAT] Kompilasi gagal.
)
if exist sources.txt del sources.txt
pause
