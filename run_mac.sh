#!/bin/bash

# Renk tanımlamaları
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # Renk sıfırlama

echo -e "${CYAN}===================================================${NC}"
echo -e "${CYAN}  Akilli Kutup Sistemi Baslatiliyor (macOS)...${NC}"
echo -e "${CYAN}===================================================${NC}"
echo

# 1. Sistem Kontrolleri (Java & Maven)
echo -e "[*] Sistem gereksinimleri kontrol ediliyor..."
if ! command -v java &> /dev/null; then
    echo -e "${RED}[HATA] Sisteminizde Java bulunamadi!${NC}"
    echo -e "${YELLOW}Lutfen Java JDK 17 veya uzeri bir surum yukleyin.${NC}"
    exit 1
fi

if ! command -v mvn &> /dev/null; then
    echo -e "${RED}[HATA] Sisteminizde Maven bulunamadi!${NC}"
    echo -e "${YELLOW}Lutfen Apache Maven yukleyin.${NC}"
    exit 1
fi
echo -e "${GREEN}[OK] Java ve Maven yuklu.${NC}"

# 2. Port Kontrolü (8080)
PORT_PID=$(lsof -t -i:8080 -sTCP:LISTEN 2>/dev/null)
if [ -n "$PORT_PID" ]; then
    echo -e "${YELLOW}[UYARI] 8080 portu su anda kullanimda (PID: $PORT_PID).${NC}"
    read -p "Bu portu kullanan surec sonlandirilsin mi? (e/h): " kill_choice
    if [[ "$kill_choice" =~ ^[Ee]$ ]]; then
        kill -9 $PORT_PID
        echo -e "${GREEN}[OK] Port serbest birakildi.${NC}"
    else
        echo -e "${RED}[IPTAL] Baslatma islemi iptal edildi. Lutfen 8080 portunu bosaltip tekrar deneyin.${NC}"
        exit 1
    fi
fi

# 3. Java Backend'i arka planda baslat
echo
echo -e "${BLUE}[1/2] Java API Sunucusu baslatiliyor...${NC}"
mvn compile exec:java -Dexec.mainClass=com.akillikutup.Main &
BACKEND_PID=$!

# Sunucunun hazir olmasi icin kısa bir sure bekle
echo "Sunucunun hazirlanmasi bekleniyor..."
sleep 5

# 4. Tarayicida arayuzu ac
echo -e "${BLUE}[2/2] Tarayici aciliyor...${NC}"
open http://localhost:8080/

echo
echo -e "${GREEN}===================================================${NC}"
echo -e "${GREEN}  Sistem hazir! Kapatmak icin Ctrl+C tuslarina basiniz.${NC}"
echo -e "${GREEN}===================================================${NC}"

# Script sonlandirildiginda java surecini de kapat
cleanup() {
    echo "Sunucu kapatiliyor..."
    kill $BACKEND_PID 2>/dev/null
    exit
}
trap cleanup SIGINT SIGTERM

# Backend loglarini takip et
wait $BACKEND_PID
