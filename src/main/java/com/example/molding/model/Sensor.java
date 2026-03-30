package com.example.molding.model;

import java.util.Random;

public class Sensor {
    private final String name;
    private final String unit;
    private final double idealValue;
    private final double noiseStdDev;
    private final Random rnd = new Random();

    public Sensor(String name, String unit, double idealValue, double noiseStdDev) {
        this.name = name;
        this.unit = unit;
        this.idealValue = idealValue;
        this.noiseStdDev = noiseStdDev;
    }

    public double read(double baseValue) {
        // Gaussian noise: nextGaussian() returns mean 0, stddev 1
        return baseValue + rnd.nextGaussian() * noiseStdDev;
    }

    public String getName() { return name; }
    public String getUnit() { return unit; }
    public double getIdealValue() { return idealValue; }
    public double getNoiseStdDev() { return noiseStdDev; }
}