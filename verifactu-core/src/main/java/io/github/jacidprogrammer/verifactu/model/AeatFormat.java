package io.github.jacidprogrammer.verifactu.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Textual formats the AEAT expects in XML fields, hash inputs and QR URLs.
 *
 * <p>Dates are {@code dd-MM-yyyy}; timestamps are ISO 8601 with offset and whole seconds
 * ({@code 2024-01-01T19:20:30+01:00}); amounts use a dot and two decimals.
 */
public final class AeatFormat {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssxxx");

    private AeatFormat() {
    }

    public static String date(LocalDate date) {
        return DATE.format(date);
    }

    public static String dateTime(OffsetDateTime dateTime) {
        return DATE_TIME.format(dateTime);
    }

    /** Two decimals, dot separator, no grouping: {@code 123.45}, {@code 241.40}. */
    public static String amount(BigDecimal amount) {
        return amount.setScale(2).toPlainString();
    }

    /**
     * Amount as printed in the QR URL: trailing zeros removed, as in the AEAT example {@code importe=241.4}.
     * {@code 241.00} becomes {@code 241}.
     */
    public static String qrAmount(BigDecimal amount) {
        BigDecimal stripped = amount.stripTrailingZeros();
        return (stripped.scale() < 0 ? stripped.setScale(0) : stripped).toPlainString();
    }
}
