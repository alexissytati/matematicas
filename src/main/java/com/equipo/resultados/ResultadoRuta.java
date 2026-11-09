package com.equipo.resultados;

import java.util.List;

public record ResultadoRuta(
        List<Integer> numerosActividades,
        List<String> nombresActividades,
        double duracion,
        double varianza,
        double desviacionEstandar,
        double valorZ,
        double probabilidad) {
    public ResultadoRuta {
        numerosActividades = List.copyOf(numerosActividades);
        nombresActividades = List.copyOf(nombresActividades);
    }
}
