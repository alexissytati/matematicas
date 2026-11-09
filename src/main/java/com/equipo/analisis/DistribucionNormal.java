package com.equipo.analisis;

/** Aproximación de la función de distribución normal estándar. */
public final class DistribucionNormal {
    private DistribucionNormal() { }

    public static double probabilidadAcumulada(double z) {
        if (Double.isNaN(z)) {
            throw new IllegalArgumentException("El valor Z no puede ser NaN.");
        }
        if (z == Double.POSITIVE_INFINITY) return 1.0;
        if (z == Double.NEGATIVE_INFINITY) return 0.0;

        double x = Math.abs(z);
        double t = 1.0 / (1.0 + 0.2316419 * x);
        double densidad = 0.3989422804014327 * Math.exp(-0.5 * x * x);
        double cola = densidad * t * (0.319381530 + t * (-0.356563782
                + t * (1.781477937 + t * (-1.821255978 + t * 1.330274429))));
        return z >= 0.0 ? 1.0 - cola : cola;
    }
}
