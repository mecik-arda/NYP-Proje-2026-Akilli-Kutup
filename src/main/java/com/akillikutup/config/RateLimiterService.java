package com.akillikutup.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Rate Limiting servisi — brute-force koruması.
 *
 * ÖNCELİKLİ: Redis (dağıtık, ölçeklenebilir)
 * FALLBACK: ConcurrentHashMap (Redis yoksa yerelde çalışır)
 *
 * Algoritma: Sabit pencere (Fixed Window) — her IP için 5 dakikada max 5 başarısız deneme.
 */
@Service
public class RateLimiterService {

    private static final Logger log = LoggerFactory.getLogger(RateLimiterService.class);

    private static final int MAX_ATTEMPTS = 5;          // Maksimum başarısız deneme
    private static final int WINDOW_MINUTES = 5;        // Pencere süresi (dakika)
    private static final String REDIS_PREFIX = "rate:login:";

    private final RedisTemplate<String, String> redisTemplate;
    private final boolean redisAvailable;

    // Redis yoksa kullanılacak fallback
    private final ConcurrentHashMap<String, int[]> fallbackStore = new ConcurrentHashMap<>();
    // [denemeSayisi, pencereBaslangicTimestamp]

    public RateLimiterService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        boolean available = false;
        try {
            redisTemplate.opsForValue().get("health-check");
            available = true;
        } catch (Exception e) {
            log.warn("Redis erişilebilir değil. In-memory rate limiting (fallback) kullanılıyor.");
        }
        this.redisAvailable = available;
    }

    /**
     * IP adresi için başarısız deneme kaydeder.
     * @return true = limit aşılmadı (denemeye devam edebilir), false = rate limit aşıldı
     */
    public boolean recordFailedAttempt(String ipAddress) {
        if (redisAvailable) {
            return recordWithRedis(ipAddress);
        } else {
            return recordWithFallback(ipAddress);
        }
    }

    /**
     * IP'nin bloke olup olmadığını kontrol eder.
     */
    public boolean isBlocked(String ipAddress) {
        return remainingAttempts(ipAddress) <= 0;
    }

    /**
     * IP'nin rate limitini sıfırlar (başarılı giriş sonrası).
     */
    public void resetAttempts(String ipAddress) {
        if (redisAvailable) {
            try {
                redisTemplate.delete(REDIS_PREFIX + ipAddress);
            } catch (Exception e) {
                log.error("Redis reset hatası: {}", e.getMessage());
            }
        } else {
            fallbackStore.remove(ipAddress);
        }
    }

    /**
     * IP'nin kaç denemesi kaldığını döndürür.
     */
    public int remainingAttempts(String ipAddress) {
        if (redisAvailable) {
            try {
                String val = redisTemplate.opsForValue().get(REDIS_PREFIX + ipAddress);
                int count = val != null ? Integer.parseInt(val) : 0;
                return Math.max(0, MAX_ATTEMPTS - count);
            } catch (Exception e) {
                return MAX_ATTEMPTS;
            }
        } else {
            int[] data = fallbackStore.get(ipAddress);
            if (data == null) return MAX_ATTEMPTS;
            long now = System.currentTimeMillis();
            long windowStart = data[1];
            if (now - windowStart > TimeUnit.MINUTES.toMillis(WINDOW_MINUTES)) {
                return MAX_ATTEMPTS;
            }
            return Math.max(0, MAX_ATTEMPTS - data[0]);
        }
    }

    // ─── Redis implementasyonu ─────────────────────────────────────

    private boolean recordWithRedis(String ipAddress) {
        try {
            String key = REDIS_PREFIX + ipAddress;
            String val = redisTemplate.opsForValue().get(key);

            if (val == null) {
                // İlk deneme: pencereyi başlat
                redisTemplate.opsForValue().set(key, "1", WINDOW_MINUTES, TimeUnit.MINUTES);
                return true;
            }

            int count = Integer.parseInt(val);
            if (count >= MAX_ATTEMPTS) {
                return false; // Limit aşıldı
            }

            // Atomik artırım (Redis INCR)
            Long newCount = redisTemplate.opsForValue().increment(key);
            // TTL'yi yenile (opsiyonel: her denemede pencere yeniden başlamasın diye yapmıyoruz)
            return newCount != null && newCount <= MAX_ATTEMPTS;
        } catch (Exception e) {
            log.error("Redis rate limit hatası, fallback'e geçiliyor: {}", e.getMessage());
            return recordWithFallback(ipAddress);
        }
    }

    // ─── ConcurrentHashMap fallback ────────────────────────────────

    private boolean recordWithFallback(String ipAddress) {
        long now = System.currentTimeMillis();
        long windowMs = TimeUnit.MINUTES.toMillis(WINDOW_MINUTES);

        int[] data = fallbackStore.compute(ipAddress, (key, current) -> {
            if (current == null || now - current[1] > windowMs) {
                // Yeni pencere başlat
                return new int[]{1, (int) now};
            }
            // Mevcut pencerede sayacı artır
            current[0]++;
            return current;
        });

        return data[0] <= MAX_ATTEMPTS;
    }
}
