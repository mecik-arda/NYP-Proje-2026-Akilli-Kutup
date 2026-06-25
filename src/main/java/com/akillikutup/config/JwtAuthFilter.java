package com.akillikutup.config;

import com.akillikutup.user.ActiveSessionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final ActiveSessionService sessionService;

    public JwtAuthFilter(JwtUtil jwtUtil, ActiveSessionService sessionService) {
        this.jwtUtil = jwtUtil;
        this.sessionService = sessionService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String token = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        // SSE bağlantıları için query parametreden token al
        if (token == null && request.getRequestURI().contains("/stream")) {
            token = request.getParameter("token");
        }

        if (token != null && !token.isEmpty()) {

            if (jwtUtil.isTokenValid(token)) {
                String userId = jwtUtil.extractUserId(token);
                String rol = jwtUtil.extractRole(token);

                List<SimpleGrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_" + (rol != null ? rol : "UYE"))
                );

                UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userId, null, authorities);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);

                // Aktivite takibi: API isteklerini oturuma işle
                String path = request.getRequestURI();
                String action = mapPathToAction(path);
                sessionService.updateActivity(userId, action);
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * API yoluna göre insan-okunur aksiyon açıklaması döndürür.
     */
    private String mapPathToAction(String path) {
        if (path.contains("/aktif-kullanicilar/aktivite")) return null; // ping kendisi
        if (path.contains("/aktif-kullanicilar/stream")) return null; // SSE stream
        if (path.contains("/aktif-kullanicilar")) return "Aktif üye listesini inceliyor";
        if (path.contains("/istatistikler")) return "İstatistikleri inceliyor";
        if (path.contains("/kitaplar")) return "Kitap kataloğunu inceliyor";
        if (path.contains("/kullanicilar")) return "Üye listesini inceliyor";
        if (path.contains("/odunc")) return "Ödünç işlemi yapıyor";
        if (path.contains("/iade")) return "İade işlemi yapıyor";
        if (path.contains("/chat")) return "AI Asistan ile konuşuyor";
        if (path.contains("/profil")) return "Profilini düzenliyor";
        if (path.contains("/sifre")) return "Şifre değiştiriyor";
        if (path.contains("/duyuru")) return "Duyuru gönderiyor";
        if (path.contains("/settings")) return "Ayarları düzenliyor";
        if (path.contains("/backup")) return "Yedekleme yapıyor";
        if (path.contains("/bildirimler")) return "Bildirimleri inceliyor";
        if (path.contains("/odunc-gecmisi")) return "Ödünç geçmişini inceliyor";
        return "Sistemi kullanıyor";
    }
}
