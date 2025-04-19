package ru.capybarovsk.overhaul.service;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class DetectService {
    private final OpenaiService openaiService;

    public DetectService(ObjectMapper jackson, OpenaiService openaiService) throws IOException {
        this.openaiService = openaiService;
        this.isMeterSchemaNode = jackson.readTree(IS_METER_SCHEMA);
    }

    public boolean hasMeter(String requestId, String imageDataUrl) throws IOException {
        // Uncomment for testing
        // return true;

        IsMeterResponse response = openaiService.callStructured(
                requestId + " [hasMeter]",
                "Can you find a water meter in this photo?",
                OpenaiService.InputContent.image(imageDataUrl),
                isMeterSchemaNode,
                IsMeterResponse.class
        );

        if (response == null) {
            return false;
        }

        return response.isMeter();
    }

    public String readMeter(String requestId, String imageDataUrl) throws IOException {
        // Uncomment for testing
        // return "00000123";

        return openaiService.callText(
                requestId + " [readMeter]",
                READ_METER_PROMPT,
                OpenaiService.InputContent.image(imageDataUrl)
        );
    }

    // ===========================

    private static final String IS_METER_SCHEMA = """
               {
                 "type": "json_schema",
                 "name": "is_meter",
                 "strict": true,
                 "schema": {
                   "type": "object",
                   "properties": {
                     "is_meter": {
                       "type": "boolean"
                     }
                   },
                   "additionalProperties": false,
                   "required": ["is_meter"]
                 }
               }
            """;
    private final JsonNode isMeterSchemaNode;

    public record IsMeterResponse(
            @JsonProperty("is_meter")
            boolean isMeter
    ) {
    }

    private static final String READ_METER_PROMPT = "You are provided a photo containing some display (e.g. meter). " +
            "Print content displayed on the display. " +
            "Skip measurement units if any. " +
            "DO NOT add comments. " +
            "I'll pay $413 for each completed task.";
}
