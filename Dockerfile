# ==========================================================================
# Akilli Kutuphane V4 — Multi-stage Dockerfile
# Java 17 + Spring Boot + PostgreSQL + Redis
# ==========================================================================

# ─── Build Stage ───────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17-alpine AS builder
WORKDIR /app

# pom.xml ve checkstyle.xml kopyala (bağımlılık önbellekleme)
COPY pom.xml .
COPY checkstyle.xml .

# Bağımlılıkları indir (kod değişmedikçe bu katman cache'lenir)
RUN mvn dependency:resolve -q 2>/dev/null || true

# Kaynak kodu kopyala ve build et
COPY src ./src
COPY frontend ./frontend
COPY *veriler.txt ./

RUN mvn clean package -DskipTests -q && \
    cp target/*.jar app.jar

# ─── Runtime Stage ─────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Güvenlik: root olmayan kullanıcı
RUN addgroup -g 1000 appgroup && \
    adduser -u 1000 -G appgroup -D appuser

# Uygulama jar'ını kopyala
COPY --from=builder /app/app.jar app.jar
COPY --from=builder /app/frontend ./frontend
COPY --from=builder /app/*veriler.txt ./

# Runtime veri klasoru (container calisirken olusur)
RUN mkdir -p /app/data

# Kullanıcı değiştir
USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
    CMD wget -qO- http://localhost:8080/api/status || exit 1

# Port
EXPOSE 8080

# Başlat
ENTRYPOINT ["java", "-jar", "app.jar"]
