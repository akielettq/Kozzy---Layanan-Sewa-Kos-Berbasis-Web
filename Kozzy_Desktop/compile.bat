@echo off
cd /d "%~dp0"
echo ========================================================
echo   Mengompilasi Kozzy Desktop GUI (PBO Workstation)
echo ========================================================
if not exist "bin" mkdir "bin"

powershell -NoProfile -Command "& javac -encoding UTF-8 -cp 'lib/*;bin' -d bin (Get-ChildItem -Path src,test -Filter *.java -Recurse | Select-Object -ExpandProperty FullName)"

if %ERRORLEVEL% EQU 0 (
    echo [SUKSES] Kompilasi berhasil tanpa galat!
) else (
    echo [GALAT] Kompilasi gagal.
)
pause
