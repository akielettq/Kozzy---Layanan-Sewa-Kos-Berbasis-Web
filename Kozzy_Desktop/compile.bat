@echo off
setlocal EnableDelayedExpansion

cd /d "%~dp0"

echo ========================================================
echo   Mengompilasi Kozzy Desktop GUI (PBO Workstation)
echo ========================================================

if not exist "bin" mkdir "bin"

if exist sources.txt del sources.txt

for /r "src" %%F in (*.java) do (
    set "file=%%~fF"
    set "file=!file:\=/!"
    echo "!file!" >> sources.txt
)

javac -encoding UTF-8 -cp "lib/*;bin" -d bin @sources.txt

if errorlevel 1 (
    echo [GALAT] Kompilasi gagal.
) else (
    echo [SUKSES] Kompilasi berhasil tanpa galat!
)

if exist sources.txt del sources.txt

pause
