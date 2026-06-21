#!/bin/bash

# Renk tanımlamaları
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # Renk sıfırlama

echo -e "${CYAN}===================================================${NC}"
echo -e "${CYAN}  Akilli Kutup V4 Baslatiliyor (macOS Docker)...${NC}"
echo -e "${CYAN}===================================================${NC}"
echo

echo -e "[*] Docker kurulumu kontrol ediliyor..."
if ! command -v docker &> /dev/null; then
    echo -e "${RED}[HATA] Sisteminizde Docker bulunamadi!${NC}"
    echo -e "${YELLOW}V4 mimarisi PostgreSQL ve Redis gerektirdiginden Docker zorunludur.${NC}"
    exit 1
fi
echo -e "${GREEN}[OK] Docker yuklu.${NC}"

echo
echo -e "${BLUE}[1/3] Konteynerler (PostgreSQL, Redis, Spring Boot) baslatiliyor...${NC}"
docker compose up --build -d

echo -e "${BLUE}[2/3] Servislerin hazirlanmasi bekleniyor (15 saniye)...${NC}"
sleep 15

echo -e "${BLUE}[3/3] Java Masaustu (Swing GUI) Uygulamasi Arkaplanda Baslatiliyor...${NC}"
nohup mvn clean compile exec:java -Dexec.mainClass=com.akillikutup.Main > gui.log 2>&1 &
echo -e "${GREEN}[OK] Masaustu GUI basariyla cagirildi.${NC}"

echo -e "${GREEN}===================================================${NC}"
echo -e "${GREEN}  Sistem hazir! Iki Arayuz de aktif!${NC}"
echo -e "${GREEN}  Web Arayuzu: http://localhost:8080${NC}"
echo -e "${GREEN}  Masaustu Arayuzu (Java GUI) ise pencere olarak acildi.${NC}"
echo -e "${GREEN}===================================================${NC}"
echo -e "Sunucu Loglari (Ctrl+C ile cikabilirsiniz):"
echo -e "Sistemi kapatmak icin: docker compose down\n"

if command -v open &> /dev/null; then
    open http://localhost:8080/
fi

docker compose logs -f app
