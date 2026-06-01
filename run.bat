@echo off
echo ===================================================
echo   Akilli Kutup Sistemi Baslatiliyor...
echo ===================================================
echo.

:: Java Backend'i baslat
echo [1/2] Java API Sunucusu baslatiliyor...
start "Akilli Kutup Backend" cmd /c "mvn clean compile exec:java -Dexec.mainClass=com.akillikutup.Main"

:: Sunucunun hazir olmasi icin kısa bir sure bekle
timeout /t 5 >nul

:: Tarayicida arayuzu ac
echo [2/2] Tarayici aciliyor...
start http://localhost:8080/

echo.
echo ===================================================
echo   Sistem hazir! Backend konsolunu kapatmayiniz.
echo ===================================================
pause
