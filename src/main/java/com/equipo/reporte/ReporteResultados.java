package com.equipo.reporte;

import com.equipo.modelo.ActividadPert;
import com.equipo.resultados.ResultadoActividad;
import com.equipo.resultados.ResultadoProyecto;
import com.equipo.resultados.ResultadoRuta;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/** Presenta los resultados del análisis sin calcularlos. */
public class ReporteResultados {
    public void imprimir(ResultadoProyecto resultado) {
        Map<Integer, String> nombres = new HashMap<>();
        for (ResultadoActividad fila : resultado.actividades()) {
            nombres.put(fila.actividad().getNumero(), fila.actividad().getNombre());
        }

        System.out.println("\nMATRIZ DE RESULTADOS PERT/CPM");
        System.out.printf(Locale.US, "%-7s %-22s %-28s %9s %9s %9s %9s %9s %9s %-9s%n",
                "Número", "Actividad", "Predecesoras", "TE", "ES", "EF", "LS", "LF", "H", "Crítica");
        System.out.println("-".repeat(130));
        for (ResultadoActividad fila : resultado.actividades()) {
            ActividadPert actividad = fila.actividad();
            String predecesoras = actividad.getPredecesoras().isEmpty() ? "—"
                    : actividad.getPredecesoras().stream().map(nombres::get)
                            .collect(Collectors.joining(", "));
            System.out.printf(Locale.US, "%-7d %-22s %-28s %9.2f %9.2f %9.2f %9.2f %9.2f %9.2f %-9s%n",
                    actividad.getNumero(), actividad.getNombre(), predecesoras,
                    fila.tiempoEsperado(), fila.inicioTemprano(), fila.finalTemprano(),
                    fila.inicioTardio(), fila.finalTardio(), fila.holgura(),
                    fila.critica() ? "Sí" : "No");
        }

        System.out.println("\nRESULTADOS GLOBALES DEL PROYECTO");
        System.out.println("Número total de actividades: " + resultado.actividades().size());
        System.out.printf(Locale.US, "Duración esperada del proyecto: %.4f%n", resultado.duracionEsperada());
        System.out.println("Número de actividades críticas: " + resultado.cantidadActividadesCriticas());
        System.out.println("Rutas críticas:");
        for (int i = 0; i < resultado.rutasCriticas().size(); i++) {
            ResultadoRuta ruta = resultado.rutasCriticas().get(i);
            String zRuta = Double.isNaN(ruta.valorZ()) ? "no definido"
                    : String.format(Locale.US, "%.4f", ruta.valorZ());
            System.out.printf(Locale.US, "  %d) %s | Duración: %.4f | Varianza: %.4f | "
                            + "Desv. estándar: %.4f | Z: %s | Probabilidad: %.2f%%%n",
                    i + 1, String.join(" → ", ruta.nombresActividades()), ruta.duracion(),
                    ruta.varianza(), ruta.desviacionEstandar(), zRuta, 100.0 * ruta.probabilidad());
        }
        System.out.printf(Locale.US, "Tiempo meta: %.4f%n", resultado.tiempoMeta());
        System.out.printf(Locale.US, "Varianza estimada del proyecto: %.4f%n", resultado.varianzaProyecto());
        System.out.printf(Locale.US, "Desviación estándar estimada: %.4f%n", resultado.desviacionEstandar());
        if (Double.isNaN(resultado.valorZ())) {
            System.out.println("Valor Z: no definido (varianza cero; duración determinista).");
        } else {
            System.out.printf(Locale.US, "Valor Z: %.4f%n", resultado.valorZ());
        }
        System.out.printf(Locale.US, "Probabilidad estimada de terminar antes o en la meta: %.2f%%%n",
                100.0 * resultado.probabilidad());
        if (resultado.rutasCriticas().size() > 1) {
            System.out.println("Nota: hay varias rutas críticas. La probabilidad global usa como aproximación "
                    + "la ruta crítica con mayor varianza; se muestran las varianzas de todas las rutas.");
        }
        System.out.println("La probabilidad es una aproximación normal PERT y supone independencia "
                + "entre las duraciones de las actividades de la ruta considerada.");
    }
}
