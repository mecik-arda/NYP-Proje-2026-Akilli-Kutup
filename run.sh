#!/bin/bash
echo "==================================================="
echo "  Akilli Kutup Sistemi Baslatiliyor..."
echo "==================================================="
echo

# Java Backend'i arka planda baslat
echo "[1/2] Java API Sunucusu baslatiliyor..."
mvn clean compile exec:java -Dexec.mainClass=com.akillikutup.Main &
BACKEND_PID=$!

# Sunucunun hazir olmasi icin kısa bir sure bekle
sleep 5

# Tarayicida arayuzu ac (macOS için open, Linux için xdg-open)
echo "[2/2] Tarayici aciliyor..."
if command -v open &> /dev/null; then
    open http://localhost:8080/
elif command -v xdg-open &> /dev/null; then
    xdg-open http://localhost:8080/
else
    echo "Lutfen tarayicinizdan http://localhost:8080/ adresine gidiniz."
fi

echo
echo "==================================================="
echo "  Sistem hazir! Kapatmak icin Ctrl+C tuslarina basiniz."
echo "==================================================="

# Script sonlandirildiginda java surecini de kapat
cleanup() {
    echo "Sunucu kapatiliyor..."
    kill $BACKEND_PID
    exit
}
trap cleanup SIGINT SIGTERM

# Backend loglarini takip et
wait $BACKEND_PID
