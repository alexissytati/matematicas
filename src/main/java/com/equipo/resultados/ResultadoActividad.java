package com.equipo.resultados;

import com.equipo.modelo.ActividadPert;

public record ResultadoActividad(
        ActividadPert actividad,
        double tiempoEsperado,
        double varianza,
        double inicioTemprano,
        double finalTemprano,
        double inicioTardio,
        double finalTardio,
        double holgura,
        boolean critica) {
}
