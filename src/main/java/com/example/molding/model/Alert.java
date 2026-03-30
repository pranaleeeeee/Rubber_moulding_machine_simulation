package com.example.molding.model;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class Alert {
    private final String severity;
    private final String message;
    private final long timestampMillis;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    public Alert(String severity, String message) {
        this.severity = severity == null ? "INFO" : severity.toUpperCase();
        this.message = message;
        this.timestampMillis = Instant.now().toEpochMilli();
    }

    public String getSeverity() { return severity; }
    public String getMessage() { return message; }
    public long getTimestampMillis() { return timestampMillis; }
    public String getFormattedTimestamp() {
        return FORMATTER.format(Instant.ofEpochMilli(timestampMillis));
    }

    @Override
    public String toString() {
        return String.format("[ALERT-%s] (%s) %s", severity, getFormattedTimestamp(), message);
    }
}