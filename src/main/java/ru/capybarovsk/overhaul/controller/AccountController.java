package ru.capybarovsk.overhaul.controller;

import jakarta.servlet.http.HttpServletResponse;
import java.util.regex.Pattern;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.capybarovsk.overhaul.dao.UserDao;
import ru.capybarovsk.overhaul.model.User;
import ru.capybarovsk.overhaul.security.CookieFactory;

@Controller
@RequestMapping("/account")
public class AccountController {
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
    private static final Pattern LOGIN_PATTERN = Pattern.compile("^[0-9]{9,}$");
    private final CookieFactory cookieFactory;
    private final UserDao userDao;

    public AccountController(CookieFactory cookieFactory, UserDao userDao) {
        this.cookieFactory = cookieFactory;
        this.userDao = userDao;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping(
            path = "/login",
            consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE}
    )
    public String login(LoginRequest loginRequest, Model model, HttpServletResponse response) {
        User user = userDao.getUserByLogin(loginRequest.login);

        if (user == null) {
            response.setStatus(403);
            model.addAttribute("error", "Неверный логин или пароль");
            return "login";
        }

        if (!encoder.matches(loginRequest.password, user.password())) {
            response.setStatus(403);
            model.addAttribute("error", "Неверный логин или пароль");
            return "login";
        }

        cookieFactory.writeCookie(response, user);
        return "redirect:/";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping(
            path = "/register",
            consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE}
    )
    public String register(RegisterRequest registerRequest, Model model, HttpServletResponse response) {
        User user = userDao.getUserByLogin(registerRequest.login());

        if (user != null) {
            response.setStatus(400);
            model.addAttribute("error", "Этот пользователь уже существует");
            return "register";
        }

        if (!LOGIN_PATTERN.asMatchPredicate().test(registerRequest.login())) {
            response.setStatus(400);
            model.addAttribute("error", "Неверный номер лицевого счёта");
            return "register";
        }

        long id = userDao.createUser(
                registerRequest.login(),
                encoder.encode(registerRequest.password()),
                registerRequest.fullName(),
                registerRequest.address()
        );

        cookieFactory.writeCookie(response, new CookieFactory.UserPrincipal(id, registerRequest.login()));

        return "redirect:/";
    }

    @GetMapping("/logout")
    public String logoutPage(HttpServletResponse response) {
        cookieFactory.deleteCookie(response);
        return "redirect:/account/login";
    }

    public record LoginRequest(String login, String password) {
    }

    public record RegisterRequest(String login, String password, String fullName, String address) {}
}
