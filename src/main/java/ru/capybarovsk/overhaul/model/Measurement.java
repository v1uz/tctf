package ru.capybarovsk.overhaul.model;

import java.util.Date;

public record Measurement(
        long id,
        String measurement,
        Date createdAt
) {}
