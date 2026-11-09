package com.equipo.resultados;

import java.util.List;

public record ResultadoProyecto(
        List<ResultadoActividad> actividades,
        double duracionEsperada,
        int cantidadActividadesCriticas,
        List<ResultadoRuta> rutasCriticas,
        double tiempoMeta,
        double varianzaProyecto,
        double desviacionEstandar,
        double valorZ,
        double probabilidad) {
    public ResultadoProyecto {
        actividades = List.copyOf(actividades);
        rutasCriticas = List.copyOf(rutasCriticas);
    }
}
