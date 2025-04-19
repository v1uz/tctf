package ru.capybarovsk.overhaul.model;

import java.math.BigDecimal;
import java.util.Date;

public record Invoice(
        long id,
        BigDecimal total,
        boolean paid,
        Date createdAt
) {}
