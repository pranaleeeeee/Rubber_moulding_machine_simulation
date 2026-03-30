package com.example.molding.model;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RubberMoldingMachine {
    private final String id = "HRM-RUBBER-001";
    private final double baselinePressingTime = 30.0;
    private final Map<String, Sensor> sensors = new LinkedHashMap<>();
    private final List<Map<String, Object>> history = new ArrayList<>();
    private final List<Alert> alerts = new ArrayList<>();
    private CycleData latestCycle = null;

    public RubberMoldingMachine() {
        initSensors();
    }

    private void initSensors() {
        sensors.put("MOLD_TEMPERATURE", new Sensor("Mold Temperature", "°C", 160.0, 0.5));
        sensors.put("MOLDING_PRESSURE", new Sensor("Molding Pressure", "bar", 200.0, 2.0));
        sensors.put("HYDRAULIC_FORCE", new Sensor("Hydraulic Force", "kN", 400.0, 5.0));
        sensors.put("VIBRATION_LEVEL", new Sensor("Vibration Level", "mm/s", 3.0, 0.05));
        sensors.put("PRESSING_TIME", new Sensor("Pressing Time", "sec", baselinePressingTime, 0.2));
        sensors.put("PUMP_MOTOR_RPM", new Sensor("Pump Motor RPM", "rpm", 1450.0, 10.0));
    }

    @PostConstruct
    public void populateBaselineHistory() {
        Random rnd = new Random();
        int N_NORMAL = 50;
        for (int i = 0; i < N_NORMAL; i++) {
            double baseMoldTemp = 155.0 + rnd.nextDouble() * 10.0;         // 155 -165
            double basePressure = 195.0 + rnd.nextDouble() * 10.0;         // 195-205
            double baseForce = 390.0 + rnd.nextDouble() * 20.0;            // 390-410
            double baseVib = 2.5 + rnd.nextDouble() * 1.0;                 // 2.5-3.5
            double baseTime = 29.5 + rnd.nextDouble() * 1.0;               // 29.5-30.5
            double baseRpm = 1430.0 + rnd.nextDouble() * 40.0;             // 1430-1470

            simulateCycle(baseMoldTemp, basePressure, baseForce, baseVib, baseTime, baseRpm);
        }
    }

    public String getId() { return id; }
    public List<Map<String, Object>> getHistory() { return Collections.unmodifiableList(history); }
    public List<Alert> getAlerts() { return Collections.unmodifiableList(alerts); }
    public CycleData getLatestCycle() { return latestCycle; }
    public double getBaselinePressingTime() { return baselinePressingTime; }

    public CycleData simulateCycle(double baseMoldTemp, double baseMoldingPressure, double baseHydraulicForce,
                                   double baseVibration, double basePressingTime, double basePumpRpm) {
        int nextId = history.size() + 1;
        CycleData cycle = new CycleData(nextId);

        cycle.getParameters().put("MOLD_TEMPERATURE", round3(sensors.get("MOLD_TEMPERATURE").read(baseMoldTemp)));
        cycle.getParameters().put("MOLDING_PRESSURE", round3(sensors.get("MOLDING_PRESSURE").read(baseMoldingPressure)));
        cycle.getParameters().put("HYDRAULIC_FORCE", round3(sensors.get("HYDRAULIC_FORCE").read(baseHydraulicForce)));
        cycle.getParameters().put("VIBRATION_LEVEL", round3(sensors.get("VIBRATION_LEVEL").read(baseVibration)));
        cycle.getParameters().put("PRESSING_TIME", round3(sensors.get("PRESSING_TIME").read(basePressingTime)));
        cycle.getParameters().put("PUMP_MOTOR_RPM", round3(sensors.get("PUMP_MOTOR_RPM").read(basePumpRpm)));

        cycle.calculateKpis(baselinePressingTime);
        latestCycle = cycle;

        history.add(cycle.toMap());
        return cycle;
    }

    public void runRuleChecks(CycleData cycle) {
        double pTimeDev = cycle.getKpis().getOrDefault("Pressing_Time_Deviation_pct", 0.0);
        double pressure = cycle.getParameters().getOrDefault("MOLDING_PRESSURE", 0.0);
        double vibration = cycle.getParameters().getOrDefault("VIBRATION_LEVEL", 0.0);
        double moldTemp = cycle.getParameters().getOrDefault("MOLD_TEMPERATURE", 0.0);
        double pumpRpm = cycle.getParameters().getOrDefault("PUMP_MOTOR_RPM", 0.0);

        if (pTimeDev > 20.0) {
            alerts.add(new Alert("WARNING", String.format("Pressing time increased by %.2f%% (may affect throughput/cure).", pTimeDev)));
        }
        if (pressure < 150.0 || pressure > 240.0) {
            alerts.add(new Alert("CRITICAL", String.format("Molding pressure abnormal: %.2f bar.", pressure)));
        }
        if (vibration > 6.0) {
            alerts.add(new Alert("CRITICAL", String.format("High vibration level: %.2f mm/s.", vibration)));
        }
        if (moldTemp < 140.0) {
            alerts.add(new Alert("WARNING", String.format("Mold temperature low: %.2f °C (risk undercure).", moldTemp)));
        } else if (moldTemp > 190.0) {
            alerts.add(new Alert("CRITICAL", String.format("Mold temperature too high: %.2f °C (risk degradation).", moldTemp)));
        }
        if (pumpRpm < 1200.0 || pumpRpm > 1600.0) {
            alerts.add(new Alert("WARNING", String.format("Pump motor RPM out-of-range: %.2f rpm.", pumpRpm)));
        }
    }

    public double[] machineHealthProbability() {
        double P_working = 0.80;
        double P_faulty = 0.20;

        boolean hasCritical = alerts.stream().anyMatch(a -> "CRITICAL".equals(a.getSeverity()));
        boolean hasWarning = alerts.stream().anyMatch(a -> "WARNING".equals(a.getSeverity()));

        double P_alert_given_working;
        double P_alert_given_faulty;

        if (alerts.isEmpty()) {
            P_alert_given_working = 0.85;
            P_alert_given_faulty = 0.20;
        } else if (hasCritical) {
            P_alert_given_working = 0.05;
            P_alert_given_faulty = 0.80;
        } else {
            P_alert_given_working = 0.30;
            P_alert_given_faulty = 0.60;
        }

        double numerator = P_alert_given_working * P_working;
        double denominator = P_alert_given_working * P_working + P_alert_given_faulty * P_faulty;

        double P_working_posterior = denominator == 0 ? 0.0 : numerator / denominator;
        double P_faulty_posterior = 1.0 - P_working_posterior;

        return new double[] {
                Math.round(P_working_posterior * 100.0 * 100.0) / 100.0,
                Math.round(P_faulty_posterior * 100.0 * 100.0) / 100.0
        };
    }

    private double round3(double v) {
        return Math.round(v * 1000.0) / 1000.0;
    }
}