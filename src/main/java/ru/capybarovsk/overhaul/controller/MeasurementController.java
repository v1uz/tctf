package ru.capybarovsk.overhaul.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import ru.capybarovsk.overhaul.dao.MeasurementDao;
import ru.capybarovsk.overhaul.model.User;
import ru.capybarovsk.overhaul.service.BotService;
import ru.capybarovsk.overhaul.service.MeasurementService;

@Controller
@RequestMapping("/measurements")
public class MeasurementController {
    private final BotService botService;
    private final MeasurementDao measurementDao;
    private final MeasurementService measurementService;

    @Value("${overhaul.recaptcha.siteKey}")
    private String recaptchaKey;

    public MeasurementController(BotService botService, MeasurementDao measurementDao,
                                 MeasurementService measurementService) {
        this.botService = botService;
        this.measurementDao = measurementDao;
        this.measurementService = measurementService;
    }

    @GetMapping
    public String index(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("measurements", measurementDao.getUserMeasurements(user.id()));

        return "measurements";
    }

    @GetMapping("/new")
    public String addMeasurementPage(Model model) {
        model.addAttribute("recaptchaKey", recaptchaKey);
        return "add-measurement";
    }

    @PostMapping(
            path = "/new",
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}
    )
    public ResponseEntity<String> addMeasurement(
            HttpServletRequest request,
            @AuthenticationPrincipal User user,
            @RequestPart MultipartFile photo,
            @RequestPart String recaptchaResponse) throws IOException {
        if (measurementDao.hasTooFrequentMeasurements()) {
            return ResponseEntity.badRequest()
                    .body("В настоящий момент передача показаний доступна только в отделении КапиЖилСервисЦентра.");
        }

        if (!botService.checkBot(recaptchaResponse, request)) {
            return ResponseEntity.badRequest()
                    .body("Вы не подтвердили, что вы не робот или отправили форму несколько раз");
        }

        try {
            measurementService.insertMeasurement(photo, user);
        } catch (MeasurementService.MeasurementException exc) {
            return ResponseEntity.badRequest()
                    .body(exc.getLocalizedMessage() + "\n\nПоказание: " + exc.getMeter());
        }

        return ResponseEntity.ok("ОК");
    }
}
