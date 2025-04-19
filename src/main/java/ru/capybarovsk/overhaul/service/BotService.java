package ru.capybarovsk.overhaul.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class BotService {
    private static final String RECAPTCHA_URL = "https://www.google.com/recaptcha/api/siteverify";

    @Value("${overhaul.recaptcha.secretKey}")
    private String recaptchaKey;

    private final ObjectMapper jackson;
    private final OkHttpClient client;

    public BotService(ObjectMapper jackson, OkHttpClient okHttpClient) {
        this.jackson = jackson;
        this.client = okHttpClient;
    }

    public boolean checkBot(String recaptchaResponse, HttpServletRequest request) throws IOException {
        String remoteIp = Objects.requireNonNullElse(request.getHeader("x-real-ip"), request.getRemoteAddr());
        RequestBody formBody = new FormBody.Builder()
                .add("secret", recaptchaKey)
                .add("response", recaptchaResponse)
                .add("remoteip", remoteIp)
                .build();

        Request httpRequest = new Request.Builder()
                .url(RECAPTCHA_URL)
                .post(formBody)
                .build();

        try (Response response = client.newCall(httpRequest).execute()) {
            ResponseBody body = response.body();
            if (body == null) {
                throw new IOException("Recaptcha returned empty body");
            }
            RecaptchaResponse result = jackson.readValue(body.charStream(), RecaptchaResponse.class);

            return result.success();
        }
    }

    public record RecaptchaResponse(
            boolean success,
            @JsonProperty(value = "challenge_ts")
            String challengeTs,
            String hostname,
            @JsonProperty(value = "error-codes", required = false)
            List<String> errorCodes
    ) {}
}
