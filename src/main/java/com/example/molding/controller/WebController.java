package com.example.molding.controller;

import com.example.molding.model.CycleData;
import com.example.molding.model.RubberMoldingMachine;
import com.example.molding.model.Alert;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
public class WebController {

    private final RubberMoldingMachine machine;

    public WebController(RubberMoldingMachine machine) {
        this.machine = machine;
    }

    @GetMapping("/")
    public String index(Model model) {
        Map<String, Object> defaults = defaultValues();
        model.addAttribute("machineId", machine.getId());
        model.addAttribute("defaults", defaults);

        if (machine.getLatestCycle() != null) {
            model.addAttribute("latest", machine.getLatestCycle().toMap());
        } else {
            model.addAttribute("latest", null);
        }

        List<Map<String, Object>> history = new ArrayList<>(machine.getHistory());
        Collections.reverse(history); // most recent first
        model.addAttribute("history", history);

        List<Map<String,String>> alerts = new ArrayList<>();
        for (Alert a : machine.getAlerts()) {
            Map<String,String> m = new HashMap<>();
            m.put("severity", a.getSeverity());
            m.put("message", a.getMessage());
            m.put("tstr", a.getFormattedTimestamp());
            alerts.add(m);
        }
        model.addAttribute("alerts", alerts);

        double[] probs = machine.machineHealthProbability();
        model.addAttribute("probWorking", probs[0]);
        model.addAttribute("probFaulty", probs[1]);

        return "index";
    }

    @PostMapping("/")
    public String runCycle(
            @RequestParam(required = false) String MOLD_TEMPERATURE,
            @RequestParam(required = false) String MOLDING_PRESSURE,
            @RequestParam(required = false) String HYDRAULIC_FORCE,
            @RequestParam(required = false) String VIBRATION_LEVEL,
            @RequestParam(required = false) String PRESSING_TIME,
            @RequestParam(required = false) String PUMP_MOTOR_RPM,
            Model model) {

        Map<String, Object> defaults = defaultValues();
        double u_mold_temp = parseDoubleOrDefault(MOLD_TEMPERATURE, (Double)defaults.get("MOLD_TEMPERATURE"));
        double u_pressure = parseDoubleOrDefault(MOLDING_PRESSURE, (Double)defaults.get("MOLDING_PRESSURE"));
        double u_force = parseDoubleOrDefault(HYDRAULIC_FORCE, (Double)defaults.get("HYDRAULIC_FORCE"));
        double u_vib = parseDoubleOrDefault(VIBRATION_LEVEL, (Double)defaults.get("VIBRATION_LEVEL"));
        double u_time = parseDoubleOrDefault(PRESSING_TIME, (Double)defaults.get("PRESSING_TIME"));
        double u_rpm = parseDoubleOrDefault(PUMP_MOTOR_RPM, (Double)defaults.get("PUMP_MOTOR_RPM"));

        CycleData live = machine.simulateCycle(u_mold_temp, u_pressure, u_force, u_vib, u_time, u_rpm);
        machine.runRuleChecks(live);

        // Redirect to GET to show updated info (Post/Redirect/Get)
        return "redirect:/";
    }

    private double parseDoubleOrDefault(String s, double def) {
        if (s == null || s.isBlank()) return def;
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private Map<String,Object> defaultValues() {
        Map<String,Object> d = new LinkedHashMap<>();
        d.put("MOLD_TEMPERATURE", 160.0);
        d.put("MOLDING_PRESSURE", 200.0);
        d.put("HYDRAULIC_FORCE", 400.0);
        d.put("VIBRATION_LEVEL", 3.0);
        d.put("PRESSING_TIME", 30.0);
        d.put("PUMP_MOTOR_RPM", 1450.0);
        return d;
    }
}