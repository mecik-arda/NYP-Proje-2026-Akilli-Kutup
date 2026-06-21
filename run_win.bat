@echo off
setlocal enabledelayedexpansion

echo ===================================================
echo    Akilli Kutup V4 Baslatiliyor (Windows Docker)...
echo ===================================================
echo.

echo [*] Docker kurulumu kontrol ediliyor...
docker -v >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo [HATA] Sisteminizde Docker bulunamadi!
    echo V4 mimarisi PostgreSQL ve Redis gerektirdiginden Docker Desktop zorunludur.
    pause
    exit /b 1
)
echo [OK] Docker yuklu.

echo [*] Docker Motoru (Daemon) calisiyor mu kontrol ediliyor...
docker info >nul 2>&1
if %ERRORLEVEL% equ 0 goto docker_running

echo [UYARI] Docker arka planda calismiyor! Docker Desktop otomatik baslatiliyor...

if not exist "C:\Program Files\Docker\Docker\Docker Desktop.exe" (
    echo [HATA] Docker Desktop.exe bulunamadi! Lutfen Docker'i manuel olarak acin.
    pause
    exit /b 1
)

start "" "C:\Program Files\Docker\Docker\Docker Desktop.exe"
echo Docker'in hazir olmasi bekleniyor (Bu islem biraz surebilir)...

:wait_docker
timeout /t 5 /nobreak >nul
docker info >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo Docker motoru hala basliyor, lutfen bekleyin...
    goto wait_docker
)
echo [OK] Docker basariyla aktif hale geldi!
goto start_containers

:docker_running
echo [OK] Docker su anda aktif.

:start_containers
echo.
echo [1/3] Konteynerler (Web Sunucusu, PostgreSQL, Redis) baslatiliyor...
docker compose up --build -d

echo [2/3] Servislerin hazirlanmasi bekleniyor (15 saniye)...
timeout /t 15 /nobreak >nul

echo [3/3] Java Masaustu (Swing GUI) Uygulamasi Baslatiliyor...
start "Masaustu Arayuzu" cmd /c "mvn clean compile exec:java -Dexec.mainClass=com.akillikutup.Main"

echo ===================================================
echo    Sistem hazir! Iki Arayuz de aktif!
echo    Web Arayuzu Tarayicida aciliyor: http://localhost:8080
echo    Masaustu Arayuzu (Java GUI) ise yeni pencerede acildi.
echo ===================================================
echo Sistemi kapatmak icin: docker compose down
echo.

:: Web arayüzünü (Yeni GUI) aç
start http://localhost:8080

:: Terminal penceresini açık tut ve logları izle
echo Canli Sunucu Loglari Izleniyor (Cikmak icin CTRL+C yapabilirsiniz):
echo -------------------------------------------------------------------
docker compose logs -f app
pause
