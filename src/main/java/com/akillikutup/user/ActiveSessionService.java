package com.akillikutup.user;

import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Aktif kullanıcı oturumlarını ve anlık aktivitelerini takip eden servis.
 * JWT stateless olduğu için, oturum bilgisi burada in-memory tutulur.
 * Kullanıcı verileri değiştirilmez — sadece anlık durum takibi yapılır.
 */
@Service
public class ActiveSessionService {

    private final Map<String, ActiveSession> sessions = new ConcurrentHashMap<>();
    private final SseService sseService;

    public ActiveSessionService(SseService sseService) {
        this.sseService = sseService;
    }

    /**
     * Her 1 dakikada bir çalışır. Son 2 dakikadır ping (aktivite bildirimi) göndermeyen
     * hayalet oturumları bellekten temizler.
     */
    @Scheduled(fixedRate = 60000)
    public void cleanupInactiveSessions() {
        Instant threshold = Instant.now().minus(2, ChronoUnit.MINUTES);
        List<String> toRemove = new ArrayList<>();
        
        for (Map.Entry<String, ActiveSession> entry : sessions.entrySet()) {
            if (entry.getValue().lastActivity.isBefore(threshold)) {
                toRemove.add(entry.getKey());
            }
        }
        
        for (String userId : toRemove) {
            ActiveSession s = sessions.remove(userId);
            if (s != null) {
                sseService.broadcastUserLeft(s.userName);
            }
        }
        if (!toRemove.isEmpty()) {
            sseService.broadcastActiveCount(sessions.size());
        }
    }

    /**
     * Kullanıcı giriş yaptığında çağrılır.
     */
    public void registerSession(String userId, String userName, String role, String ipAddress) {
        ActiveSession session = new ActiveSession();
        session.userId = userId;
        session.userName = userName;
        session.role = role;
        session.ipAddress = ipAddress;
        session.loginTime = Instant.now();
        session.lastActivity = Instant.now();
        session.currentAction = "Gösterge panelini görüntülüyor";
        sessions.put(userId, session);
    }

    /**
     * Kullanıcı çıkış yaptığında veya token expire olduğunda çağrılır.
     */
    public void removeSession(String userId) {
        sessions.remove(userId);
    }

    /**
     * Tüm oturumları kapatır (admin aksiyonu).
     */
    public void terminateAllSessions() {
        sessions.clear();
    }

    /**
     * Kullanıcının son aktivite zamanını ve aksiyonunu günceller.
     */
    public void updateActivity(String userId, String action) {
        ActiveSession session = sessions.get(userId);
        if (session != null) {
            session.lastActivity = Instant.now();
            if (action != null && !action.isEmpty()) {
                session.currentAction = action;
            }
        }
    }

    /**
     * Aktif kullanıcı sayısını döndürür.
     */
    public int getActiveCount() {
        return sessions.size();
    }

    /**
     * Tüm aktif oturumları döndürür (dışa aktarım ve listeleme için).
     */
    public List<ActiveSession> getAllActiveSessions() {
        return sessions.values().stream()
            .sorted(Comparator.comparing(s -> s.userName, String.CASE_INSENSITIVE_ORDER))
            .collect(Collectors.toList());
    }

    /**
     * Belirli bir kullanıcı için aktif oturum bilgisini döndürür.
     */
    public Optional<ActiveSession> getSession(String userId) {
        return Optional.ofNullable(sessions.get(userId));
    }

    /**
     * Aktif kullanıcı isimlerini virgülle birleştirilmiş olarak döndürür (tooltip için).
     */
    public String getActiveUserNamesSummary() {
        return sessions.values().stream()
            .map(s -> s.userName)
            .limit(5)
            .collect(Collectors.joining(", "));
    }

    /**
     * Aktif kullanıcı sayısını içeren özet string.
     */
    public String getActiveUserTooltipText() {
        List<String> names = sessions.values().stream()
            .map(s -> s.userName)
            .limit(3)
            .collect(Collectors.toList());
        int remaining = sessions.size() - names.size();
        if (sessions.isEmpty()) {
            return "Şu an aktif kullanıcı yok";
        }
        String joined = String.join(", ", names);
        if (remaining > 0) {
            return joined + " ve " + remaining + " kişi daha şu an online";
        }
        return joined + " şu an online";
    }

    // ─── Inner class ────────────────────────────────────────────

    public static class ActiveSession {
        public String userId;
        public String userName;
        public String role;
        public String ipAddress;
        public Instant loginTime;
        public Instant lastActivity;
        public String currentAction;

        public Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("userId", userId);
            map.put("userName", userName);
            map.put("role", role);
            map.put("loginTime", loginTime != null ? loginTime.toString() : null);
            map.put("lastActivity", lastActivity != null ? lastActivity.toString() : null);
            map.put("currentAction", currentAction);
            return map;
        }
    }
}
