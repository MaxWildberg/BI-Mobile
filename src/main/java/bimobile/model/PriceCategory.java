package bimobile.model;

public enum PriceCategory {
    SMALL_CAR (40.0),
    SALOON (55.0),
    VAN (65.0),
    SPORTS_CAR (100.0);

    private final double baseRate;

    PriceCategory(double baseRate) {
        this.baseRate = baseRate;
    }

    public double getBaseRate() {
        return baseRate;
    }
}