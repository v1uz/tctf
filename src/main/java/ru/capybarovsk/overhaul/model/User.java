package ru.capybarovsk.overhaul.model;

import java.math.BigDecimal;

public record User(
        Long id,
        String login,
        String password,
        String clientName,
        String clientAddress,
        BigDecimal tariff
) {}
