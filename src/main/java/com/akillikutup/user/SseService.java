package com.akillikutup.user;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SSE (Server-Sent Events) servisi.
 * Aktif kullanıcı sayısı, aksiyon değişimleri gibi olayları
 * bağlı tüm frontend istemcilerine anlık olarak iletir.
 */
@Service
public class SseService {

    private final Set<SseEmitter> emitters = ConcurrentHashMap.newKeySet();

    /**
     * Yeni bir SSE bağlantısı oluşturur.
     * Frontend bu endpoint'e bağlanarak canlı güncellemeleri alır.
     */
    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(0L); // 0 = no timeout
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));

        // İlk bağlantı başarılı mesajı
        try {
            emitter.send(SseEmitter.event()
                .name("connected")
                .data(Map.of("message", "SSE bağlantısı kuruldu")));
        } catch (IOException e) {
            emitters.remove(emitter);
        }

        return emitter;
    }

    /**
     * Aktif kullanıcı sayısı değiştiğinde tüm istemcilere bildirir.
     */
    public void broadcastActiveCount(int count) {
        broadcast("activeCount", Map.of(
            "count", count,
            "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * Aktif kullanıcı listesi değiştiğinde tüm istemcilere bildirir.
     */
    public void broadcastActiveUsers(Object activeUsers) {
        broadcast("activeUsers", Map.of(
            "users", activeUsers,
            "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * Yeni bir kullanıcı giriş yaptığında bildirir.
     */
    public void broadcastUserJoined(String userName, String action) {
        broadcast("userJoined", Map.of(
            "userName", userName,
            "action", action,
            "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * Kullanıcı çıkış yaptığında bildirir.
     */
    public void broadcastUserLeft(String userName) {
        broadcast("userLeft", Map.of(
            "userName", userName,
            "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * Genel event broadcast.
     */
    public void broadcast(String eventName, Object data) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                    .name(eventName)
                    .data(data));
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }

    /**
     * Aktif bağlantı sayısını döndürür.
     */
    public int getSubscriberCount() {
        return emitters.size();
    }
}
