package com.example.molding.model;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

public class CycleData {
    private final int cycleId;
    private final long timestampMillis;
    private final Map<String, Double> parameters = new LinkedHashMap<>();
    private final Map<String, Double> kpis = new LinkedHashMap<>();

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    public CycleData(int cycleId) {
        this.cycleId = cycleId;
        this.timestampMillis = Instant.now().toEpochMilli();
    }

    public int getCycleId() { return cycleId; }
    public long getTimestampMillis() { return timestampMillis; }
    public String getFormattedTimestamp() { return FORMATTER.format(Instant.ofEpochMilli(timestampMillis)); }

    public Map<String, Double> getParameters() { return parameters; }
    public Map<String, Double> getKpis() { return kpis; }

    public void calculateKpis(double baselinePressingTime) {
        double pressingTime = parameters.getOrDefault("PRESSING_TIME", baselinePressingTime);
        double moldingPressure = parameters.getOrDefault("MOLDING_PRESSURE", 0.0);

        double timeDevPct = (pressingTime - baselinePressingTime) / Math.max(baselinePressingTime, 1e-6) * 100.0;
        kpis.put("Pressing_Time_Deviation_pct", Math.round(timeDevPct * 100.0) / 100.0);
        kpis.put("Energy_Index", Math.round(moldingPressure * pressingTime * 100.0) / 100.0);
    }

    // helper map for rendering history rows
    public Map<String, Object> toMap() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("Cycle", cycleId);
        m.put("tstr", getFormattedTimestamp());
        m.putAll(parameters);
        m.putAll(kpis);
        return m;
    }
}