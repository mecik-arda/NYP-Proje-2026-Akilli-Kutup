@echo off
setlocal enabledelayedexpansion

:: Guvenilir ANSI ESC karakteri olusturma
for /F %%a in ('echo prompt $E ^| cmd') do set "ESC=%%a"
set "RED=!ESC![91m"
set "GREEN=!ESC![92m"
set "YELLOW=!ESC![93m"
set "BLUE=!ESC![94m"
set "CYAN=!ESC![96m"
set "RESET=!ESC![0m"

echo !CYAN!===================================================!RESET!
echo !CYAN!   Akilli Kutup Sistemi Baslatiliyor (Windows)...!RESET!
echo !CYAN!===================================================!RESET!
echo.

:: 1. Sistem Kontrolleri (Java & Maven)
echo [*] Sistem gereksinimleri kontrol ediliyor...
where java >nul 2>nul
if %errorlevel% neq 0 (
    echo !RED![HATA] Sisteminizde Java bulunamadi. !RESET!
    echo !YELLOW!Lutfen Java JDK 17 veya uzeri bir surum yukleyin ve PATH ortam degiskenine ekleyin.!RESET!
    pause
    exit /b 1
)

where mvn >nul 2>nul
if %errorlevel% neq 0 (
    echo !RED![HATA] Sisteminizde Maven bulunamadi. !RESET!
    echo !YELLOW!Lutfen Apache Maven yukleyin ve PATH ortam degiskenine ekleyin.!RESET!
    pause
    exit /b 1
)
echo !GREEN![OK] Java ve Maven yuklu.!RESET!

:: 2. Port Kontrolu (8080)
set "PORT_PID="
for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":8080" ^| findstr "LISTENING"') do (
    set "PORT_PID=%%a"
)

if defined PORT_PID (
    echo !YELLOW![UYARI] 8080 portu baska bir uygulama tarafindan kullaniliyor. PID: %PORT_PID% !RESET!
    set /p "kill_choice=Bu portu kullanan surec sonlandirilsin mi? [E/H]: "
    if /i "!kill_choice!"=="E" (
        taskkill /F /PID %PORT_PID% >nul 2>&1
        echo !GREEN![OK] Port serbest birakildi.!RESET!
    ) else (
        echo !RED![IPTAL] Baslatma islemi iptal edildi. Lutfen 8080 portunu bosaltip tekrar deneyin.!RESET!
        pause
        exit /b 1
    )
)

:: 3. Java Backend'i Baslat
echo.
echo !BLUE![1/2] Java API Sunucusu baslatiliyor...!RESET!
:: Hata durumunda pencerenin acik kalmasi icin "cmd /k" kullanilmistir.
start "Akilli Kutup Backend" cmd /k "mvn compile exec:java -Dexec.mainClass=com.akillikutup.Main"

:: Sunucunun hazir olmasi icin kisa bir sure bekle
echo Sunucunun hazirlanmasi bekleniyor...
ping 127.0.0.1 -n 6 >nul

:: 4. Tarayicida Arayuzu Ac
echo !BLUE![2/2] Tarayici aciliyor...!RESET!
start http://localhost:8080/

echo.
echo !GREEN!===================================================!RESET!
echo !GREEN!   Sistem hazir. Backend konsolunu kapatmayiniz.   !RESET!
echo !GREEN!===================================================!RESET!
pause
