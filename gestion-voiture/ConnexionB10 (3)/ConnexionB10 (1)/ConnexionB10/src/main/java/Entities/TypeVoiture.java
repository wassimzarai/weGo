package Entities;

public enum TypeVoiture {
    SEDAN, SUV, COUPE, CONVERTIBLE, HATCHBACK;

    // Optionally, you can add a method to get an enum by id if needed
    public static TypeVoiture fromInt(int i) {
        switch (i) {
            case 0: return SEDAN;
            case 1: return SUV;
            case 2: return COUPE;
            case 3: return CONVERTIBLE;
            case 4: return HATCHBACK;
            default: throw new IllegalArgumentException("Unknown id: " + i);
        }
    }
}
