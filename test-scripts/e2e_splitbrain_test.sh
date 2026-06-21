#!/bin/bash
# ==========================================================================
# Akilli Kutuphane V4 — E2E Split-Brain Veri Bütünlüğü Testi
#
# Bu test şunları doğrular:
#   1. Web (curl) → kullanıcı/kitap ekleme → PostgreSQL
#   2. Swing (ApiClient simülasyonu) → aynı veriyi okuyabilme
#   3. JWT token ile ödünç alma + iade etme
#   4. Ödünç geçmişi ve istatistiklerin güncelliği
#   5. Swing ve Web'in TEK veritabanını kullandığının kanıtı
#
# KULLANIM:
#   1. Spring Boot'u başlat:  mvn spring-boot:run
#   2. Bu scripti çalıştır:   bash test-scripts/e2e_splitbrain_test.sh
# ==========================================================================

set -e

BASE_URL="http://localhost:8080"
PASS=0
FAIL=0

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

pass() { echo -e "  ${GREEN}✅${NC} $1"; PASS=$((PASS + 1)); }
fail() { echo -e "  ${RED}❌${NC} $1"; FAIL=$((FAIL + 1)); }

echo "═══════════════════════════════════════════════════════════"
echo "  Akilli Kutuphane V4 — E2E Split-Brain Testi"
echo "  Swing (ApiClient) ↔ PostgreSQL ↔ Web (curl)"
echo "═══════════════════════════════════════════════════════════"
echo ""

# ── Sunucu kontrolü ─────────────────────────────────────────────
echo "0️⃣  Sunucu kontrolü..."
if ! curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/api/status" | grep -q "200"; then
  echo -e "  ${RED}❌ Spring Boot çalışmıyor! Önce başlatın: mvn spring-boot:run${NC}"
  exit 1
fi
pass "Sunucu aktif: $BASE_URL"

# ── Admin girişi ─────────────────────────────────────────────────
echo "1️⃣  Admin girişi (JWT token)..."
ADMIN_RESP=$(curl -s -X POST "$BASE_URL/api/giris" \
  -H "Content-Type: application/json" \
  -d '{"tcKimlikNo":"11111111111","sifre":"12345678"}')

ADMIN_TOKEN=$(echo "$ADMIN_RESP" | python3 -c "import sys,json; print(json.load(sys.stdin).get('token',''))" 2>/dev/null)

if [ -z "$ADMIN_TOKEN" ]; then
  # DataInitializer çalışmamış olabilir, manuel admin oluşturmayı dene
  echo "  ${YELLOW}⚠️  Varsayılan admin yok. Seed data bekleniyor...${NC}"
  fail "Admin token alınamadı. DataInitializer çalıştı mı?"
  exit 1
fi
pass "Admin JWT token alındı"

# ── WEB'den kullanıcı ekleme ─────────────────────────────────────
echo "2️⃣  WEB (curl) → Kullanıcı ekleme..."
USER_RESP=$(curl -s -X POST "$BASE_URL/api/kullanicilar" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{"isim":"E2E Test Kullanicisi","tcKimlikNo":"55555555555","email":"e2e@test.local","rol":"uye","sifre":"e2etest123"}')

if echo "$USER_RESP" | grep -q "basarili"; then
  pass "Kullanıcı WEB'den eklendi → PostgreSQL"
else
  fail "Kullanıcı eklenemedi: $USER_RESP"
fi

# ── API listesinde doğrulama ─────────────────────────────────────
echo "3️⃣  API → Kullanıcı listesinde doğrulama..."
USERS=$(curl -s "$BASE_URL/api/kullanicilar" -H "Authorization: Bearer $ADMIN_TOKEN")
if echo "$USERS" | grep -q "E2E Test"; then
  pass "Kullanıcı API listesinde görünüyor"
else
  fail "Kullanıcı API'de yok"
fi

# ── WEB'den kitap ekleme ─────────────────────────────────────────
echo "4️⃣  WEB (curl) → Kitap ekleme..."
BOOK_RESP=$(curl -s -X POST "$BASE_URL/api/kitaplar" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{"baslik":"E2E SplitBrain Test Kitabi","yazar":"Test Yazar","kategori":"Test","stokAdedi":10,"birimFiyat":150.0,"isbn":"978-5555555555"}')

if echo "$BOOK_RESP" | grep -q "basarili"; then
  pass "Kitap WEB'den eklendi → PostgreSQL"
else
  fail "Kitap eklenemedi: $BOOK_RESP"
fi

# ── Swing ApiClient simülasyonu: API'den kitap okuma ────────────
echo "5️⃣  Swing (ApiClient simülasyonu) → Kitap listesi..."
BOOKS=$(curl -s "$BASE_URL/api/kitaplar")
E2E_COUNT=$(echo "$BOOKS" | python3 -c "
import sys, json
books = json.load(sys.stdin)
e2e = [b for b in books if 'E2E SplitBrain' in b.get('baslik','')]
print(len(e2e))
" 2>/dev/null)

if [ "$E2E_COUNT" -ge 1 ] 2>/dev/null; then
  pass "Swing ApiClient kitabı görüyor → Split-Brain YOK ✅"
else
  fail "Swing kitabı göremiyor → Split-Brain DEVAM EDİYOR"
fi

# ── Üye girişi (JWT) ────────────────────────────────────────────
echo "6️⃣  Üye girişi (JWT)..."
UYE_RESP=$(curl -s -X POST "$BASE_URL/api/giris" \
  -H "Content-Type: application/json" \
  -d '{"tcKimlikNo":"55555555555","sifre":"e2etest123"}')

UYE_TOKEN=$(echo "$UYE_RESP" | python3 -c "import sys,json; print(json.load(sys.stdin).get('token',''))" 2>/dev/null)
UYE_ID=$(echo "$UYE_RESP" | python3 -c "import sys,json; print(json.load(sys.stdin).get('id',''))" 2>/dev/null)

if [ -n "$UYE_TOKEN" ]; then
  pass "Üye JWT token alındı"
else
  fail "Üye token alınamadı"
fi

# ── Ödünç alma ──────────────────────────────────────────────────
echo "7️⃣  Ödünç alma (JWT ile)..."
BID=$(echo "$BOOKS" | python3 -c "import sys,json; arr=json.load(sys.stdin); [print(b['id']) for b in arr if 'E2E SplitBrain' in b.get('baslik','')]" 2>/dev/null | head -1)

BORROW_RESP=$(curl -s -X POST "$BASE_URL/api/odunc" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $UYE_TOKEN" \
  -d "{\"userId\":\"$UYE_ID\",\"bookId\":\"$BID\"}")

if echo "$BORROW_RESP" | grep -q "basarili"; then
  ODUNC_TARIH=$(echo "$BORROW_RESP" | python3 -c "import sys,json; print(json.load(sys.stdin).get('oduncTarihi',''))" 2>/dev/null)
  IADE_TARIH=$(echo "$BORROW_RESP" | python3 -c "import sys,json; print(json.load(sys.stdin).get('iadeTarihi',''))" 2>/dev/null)
  pass "Kitap ödünç alındı (Tarih: $ODUNC_TARIH → İade: $IADE_TARIH)"
else
  fail "Ödünç alma başarısız: $BORROW_RESP"
fi

# ── Ödünç geçmişi ───────────────────────────────────────────────
echo "8️⃣  Ödünç geçmişi kontrolü..."
HISTORY=$(curl -s "$BASE_URL/api/odunc-gecmisi" -H "Authorization: Bearer $UYE_TOKEN")
if echo "$HISTORY" | grep -q "E2E SplitBrain"; then
  pass "Ödünç geçmişinde görünüyor"
else
  fail "Ödünç geçmişte yok"
fi

# ── İstatistikler ───────────────────────────────────────────────
echo "9️⃣  İstatistikler güncel mi?"
STATS=$(curl -s "$BASE_URL/api/istatistikler" -H "Authorization: Bearer $UYE_TOKEN")
AKTIF=$(echo "$STATS" | python3 -c "import sys,json; print(json.load(sys.stdin).get('aktifOdunc',0))" 2>/dev/null)
if [ "$AKTIF" -gt 0 ] 2>/dev/null; then
  pass "Aktif ödünç sayısı: $AKTIF (istatistikler güncel)"
else
  fail "İstatistikler güncel değil"
fi

# ── Kategori dağılımı kontrolü ──────────────────────────────────
echo "🔟 Kategori dağılımı kontrolü..."
KATEGORI=$(echo "$STATS" | python3 -c "import sys,json; d=json.load(sys.stdin); print(json.dumps(d.get('kategoriDagilimi',{}), ensure_ascii=False))" 2>/dev/null)
if [ -n "$KATEGORI" ] && [ "$KATEGORI" != "{}" ]; then
  pass "Kategori dağılımı mevcut: $KATEGORI"
else
  fail "Kategori dağılımı yok"
fi

# ── İade etme ───────────────────────────────────────────────────
echo "11️⃣ İade etme..."
RETURN_RESP=$(curl -s -X POST "$BASE_URL/api/iade" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $UYE_TOKEN" \
  -d "{\"userId\":\"$UYE_ID\",\"bookId\":\"$BID\"}")

if echo "$RETURN_RESP" | grep -q "basarili"; then
  CEZA=$(echo "$RETURN_RESP" | python3 -c "import sys,json; print(json.load(sys.stdin).get('ceza',0))" 2>/dev/null)
  pass "Kitap iade edildi (Gecikme cezası: ${CEZA} TL)"
else
  fail "İade başarısız: $RETURN_RESP"
fi

# ── Yetkisiz erişim testleri ────────────────────────────────────
echo "12️⃣ Güvenlik: Yetkisiz erişim testi..."

# Kitap ekleme (token yok)
C1=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE_URL/api/kitaplar" \
  -H "Content-Type: application/json" \
  -d '{"baslik":"HACK DENEMESI"}')
[ "$C1" = "403" ] && pass "Kitap ekleme TOKEN YOK → 403 (engellendi)" || fail "Kitap ekleme TOKEN YOK → $C1 (açık kapı!)"

# Kullanıcı listesi (token yok)
C2=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/api/kullanicilar")
[ "$C2" = "403" ] && pass "Kullanıcı listesi TOKEN YOK → 403 (engellendi)" || fail "Kullanıcı listesi TOKEN YOK → $C2 (açık kapı!)"

# Status (herkese açık)
C3=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/api/status")
[ "$C3" = "200" ] && pass "Status herkese açık → 200 (doğru)" || fail "Status → $C3"

echo ""
echo "═══════════════════════════════════════════════════════════"
TOTAL=$((PASS + FAIL))
echo "  E2E TEST SONUCU: $PASS / $TOTAL BAŞARILI"
if [ "$FAIL" -eq 0 ]; then
  echo -e "  ${GREEN}✅✅✅ TÜM TESTLER BAŞARILI — SPLIT-BRAIN ÇÖZÜLDÜ ✅✅✅${NC}"
else
  echo -e "  ${RED}❌ $FAIL ADET HATA VAR${NC}"
fi
echo "  Swing (ApiClient) ↔ PostgreSQL ↔ Web (curl)"
echo "  Tek veritabanı, tek JWT, çift arayüz."
echo "═══════════════════════════════════════════════════════════"

exit $FAIL
