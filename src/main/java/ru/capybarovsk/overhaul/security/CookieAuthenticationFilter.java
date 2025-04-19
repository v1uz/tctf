package ru.capybarovsk.overhaul.security;

import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import ru.capybarovsk.overhaul.dao.UserDao;
import ru.capybarovsk.overhaul.model.User;

public class CookieAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(CookieAuthenticationFilter.class);

    private final CookieFactory cookieFactory;
    private final UserDao userDao;

    public CookieAuthenticationFilter(CookieFactory cookieFactory, UserDao userDao) {
        this.cookieFactory = cookieFactory;
        this.userDao = userDao;
    }

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request,
                                    @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain)
            throws ServletException, IOException {
        CookieFactory.UserPrincipal userPrincipal = cookieFactory.getAuthenticatedUser(request);

        if (userPrincipal != null) {
            User user = userDao.getUserById(userPrincipal.id());
            if (user != null && user.login().equals(userPrincipal.login())) {
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        filterChain.doFilter(request, response);
    }
}

