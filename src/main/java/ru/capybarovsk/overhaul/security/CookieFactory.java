package ru.capybarovsk.overhaul.security;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Base64;
import ru.capybarovsk.overhaul.model.User;
import ru.capybarovsk.overhaul.util.CookieUtil;

@Component
public class CookieFactory {
    private final static String COOKIE_NAME = "session";
    private final static Duration VALIDITY = Duration.ofDays(30);

    private final SecretKey secretKey;

    public CookieFactory(@Value("${overhaul.secret-key}") String secretKey) {
        this.secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretKey));
    }

    public void writeCookie(HttpServletResponse response, User user) {
        writeCookie(response, new UserPrincipal(user.id(), user.login()));
    }

    public void writeCookie(HttpServletResponse response, UserPrincipal userPrincipal) {
        String token = createToken(userPrincipal);
        Cookie cookie = new Cookie(COOKIE_NAME, token);
        cookie.setMaxAge(Math.toIntExact(VALIDITY.getSeconds()));
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    public void deleteCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(COOKIE_NAME, null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    private String createToken(UserPrincipal userPrincipal) {
        Claims claims = Jwts.claims()
                .id(String.valueOf(userPrincipal.id()))
                .subject(userPrincipal.login())
                .build();
        Date now = new Date();
        Date validity = Date.from(now.toInstant().plus(VALIDITY));

        return Jwts.builder().claims(claims).issuedAt(now).expiration(validity).signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    @Nullable
    public UserPrincipal getAuthenticatedUser(HttpServletRequest request) {
        String token = CookieUtil.getCookie(request, COOKIE_NAME);

        if (token == null || token.isBlank()) {
            return null;
        }

        try {
            Claims claims = parseToken(token);
            return new UserPrincipal(Long.parseLong(claims.getId()), claims.getSubject());
        } catch (JwtException e) {
            return null;
        }
    }

    private Claims parseToken(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public record UserPrincipal(
            long id,
            String login
    ) {}
}

