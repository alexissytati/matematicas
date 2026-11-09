package com.equipo.calculos;

import com.equipo.modelo.ActividadPert;

public class TiempoEsperado {

    public double calcular(ActividadPert actividad) {

        return (actividad.getOptimista()
                + 4 * actividad.getMasProbable()
                + actividad.getPesimista()) / 6;
    }
}
