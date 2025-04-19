package ru.capybarovsk.overhaul.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OpenaiService {
    private static final Logger logger = LoggerFactory.getLogger(OpenaiService.class);
    private static final String ENDPOINT = "https://api.openai.com/v1/responses";
    private static final String MODEL = "gpt-4.1-nano";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    @Value("${overhaul.openaiKey}")
    private String openaiKey;

    private final ObjectMapper jackson;
    private final OkHttpClient client;

    public OpenaiService(ObjectMapper jackson, OkHttpClient client) {
        this.jackson = jackson;
        this.client = client;
    }

    public <T> T callStructured(
            String requestName,
            String systemPrompt,
            InputContent userPrompt,
            JsonNode schema,
            Class<T> clazz
    ) throws IOException {
        ChatRequest request = new ChatRequest(
                MODEL,
                List.of(
                        new Message(
                                "system",
                                List.of(InputContent.text(systemPrompt))
                        ),
                        new Message(
                                "user",
                                List.of(userPrompt)
                        )
                ),
                new Text(schema),
                50
        );

        ChatResponse response = call(request);

        logger.info("{}: {}", requestName, response.usage());

        if (!"completed".equals(response.status())) {
            logger.warn("{}: GPT request failed: {}", requestName, response);
            return null;
        }

        final String structuredOutput;
        try {
            structuredOutput = response.extractOutput();
        } catch (Refusal refusal) {
            logger.warn("{}: GPT refused to answer: {}", requestName, refusal.getMessage());
            return null;
        }

        try {
            return jackson.readValue(structuredOutput, clazz);
        } catch (JacksonException exc) {
            logger.warn("{}: Can't parse GPT output: {}", requestName, structuredOutput, exc);
            return null;
        }
    }

    public String callText(
            String requestName,
            String systemPrompt,
            InputContent userPrompt
    ) throws IOException {
        ChatRequest request = new ChatRequest(
                MODEL,
                List.of(
                        new Message(
                                "system",
                                List.of(InputContent.text(systemPrompt))
                        ),
                        new Message(
                                "user",
                                List.of(userPrompt)
                        )
                ),
                null,
                50
        );

        ChatResponse response = call(request);

        logger.info("{}: spending: {}", requestName, response.usage());

        if (!"completed".equals(response.status())) {
            logger.warn("{}: GPT request failed: {}", requestName, response);
            return null;
        }

        try {
            return response.extractOutput();
        } catch (Refusal refusal) {
            logger.warn("{}: GPT refused to answer: {}", requestName, refusal.getMessage());
            return null;
        }
    }

    private ChatResponse call(ChatRequest request) throws IOException {
        RequestBody requestBody = RequestBody.create(
                jackson.writeValueAsString(request),
                JSON
        );

        Request httpRequest = new Request.Builder()
                .url(ENDPOINT)
                .header("Authorization", "Bearer " + openaiKey)
                .post(requestBody).build();

        try (Response response = client.newCall(httpRequest).execute()) {
            ResponseBody body = response.body();

            if (body == null) {
                throw new IOException("ChatGPT returned empty response");
            }

            return jackson.readValue(body.string(), ChatResponse.class);
        }
    }

    public record ChatRequest(
            String model,
            List<Message> input,
            @JsonInclude(JsonInclude.Include.NON_NULL)
            Text text,
            @JsonProperty("max_output_tokens")
            @JsonInclude(JsonInclude.Include.NON_NULL)
            Integer maxOutputTokens
    ) {
    }

    public record Text(
            JsonNode format
    ) {
    }

    public record Message(
            String role,
            List<InputContent> content
    ) {
    }

    public record InputContent(
            String type,
            @JsonInclude(JsonInclude.Include.NON_EMPTY)
            String text,
            @JsonProperty("image_url")
            @JsonInclude(JsonInclude.Include.NON_EMPTY)
            String imageUrl
    ) {
        public static InputContent text(String text) {
            return new InputContent("input_text", text, null);
        }

        public static InputContent image(String dataUrl) {
            return new InputContent("input_image", null, dataUrl);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ChatResponse(
            String status,
            List<Output> output,
            Usage usage,
            ChatError error,
            @JsonProperty("incomplete_details")
            IncompleteDetails incompleteDetails
    ) {
        public String extractOutput() throws Refusal {
            List<OutputContent> outputs = output().stream()
                    .flatMap(output -> output.content().stream())
                    .toList();

            Optional<OutputContent> refusal = outputs.stream()
                    .filter(output -> "refusal".equals(output.type()))
                    .findAny();

            if (refusal.isPresent()) {
                throw new Refusal(refusal.get().text());
            }

            return outputs.stream()
                    .map(output -> output.text().replace('\n', ' '))
                    .collect(Collectors.joining(" "));
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ChatError(
            String type,
            String code,
            String message
    ) {
    }

    public record IncompleteDetails(
            String reason
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Usage(
            @JsonProperty("input_tokens")
            long inputTokens,
            @JsonProperty("output_tokens")
            long outputTokens,
            @JsonProperty("total_tokens")
            long totalTokens
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Output(
            List<OutputContent> content
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OutputContent(
            String type,
            @JsonInclude(JsonInclude.Include.NON_NULL)
            String text,
            @JsonInclude(JsonInclude.Include.NON_NULL)
            String refusal
    ) {
    }

    public static class Refusal extends Exception {
        private final String message;

        public Refusal(String message) {
            super(message);
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}