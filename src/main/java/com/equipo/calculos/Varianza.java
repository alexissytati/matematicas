
package com.equipo.calculos;

import com.equipo.modelo.ActividadPert;

public class Varianza {

    public double calcular(ActividadPert actividad) {

        double diferencia = actividad.getPesimista()
                           - actividad.getOptimista();

        return Math.pow(diferencia / 6, 2);
    }
}
