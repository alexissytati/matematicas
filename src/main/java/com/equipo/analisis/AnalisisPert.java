package com.equipo.analisis;

import com.equipo.calculos.TiempoEsperado;
import com.equipo.calculos.Varianza;
import com.equipo.modelo.ActividadPert;
import com.equipo.resultados.ResultadoActividad;
import com.equipo.resultados.ResultadoProyecto;
import com.equipo.resultados.ResultadoRuta;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

/** Valida la red y calcula el análisis PERT/CPM completo. */
public class AnalisisPert {
    private static final double TOLERANCIA = 1e-9;
    private final TiempoEsperado calculadorTE = new TiempoEsperado();
    private final Varianza calculadorVarianza = new Varianza();

    public ResultadoProyecto analizar(List<ActividadPert> entrada, double tiempoMeta) {
        if (entrada == null || entrada.isEmpty()) {
            throw new IllegalArgumentException("El proyecto debe tener al menos una actividad.");
        }
        if (!Double.isFinite(tiempoMeta) || tiempoMeta < 0) {
            throw new IllegalArgumentException("El tiempo meta debe ser finito y no negativo.");
        }

        Map<Integer, ActividadPert> porNumero = new LinkedHashMap<>();
        Set<String> nombres = new HashSet<>();
        for (ActividadPert actividad : entrada) {
            if (actividad == null) throw new IllegalArgumentException("El proyecto contiene una actividad nula.");
            if (actividad.getNumero() <= 0) {
                throw new IllegalArgumentException("Cada actividad del proyecto debe tener un número positivo.");
            }
            if (porNumero.putIfAbsent(actividad.getNumero(), actividad) != null) {
                throw new IllegalArgumentException("Número de actividad duplicado: " + actividad.getNumero());
            }
            if (!nombres.add(actividad.getNombre().toLowerCase(java.util.Locale.ROOT))) {
                throw new IllegalArgumentException("Nombre de actividad duplicado: " + actividad.getNombre());
            }
        }

        Map<Integer, List<Integer>> sucesoras = new HashMap<>();
        Map<Integer, Integer> gradosEntrada = new HashMap<>();
        for (int id : porNumero.keySet()) {
            sucesoras.put(id, new ArrayList<>());
            gradosEntrada.put(id, 0);
        }
        for (ActividadPert actividad : entrada) {
            Set<Integer> unicas = new HashSet<>();
            for (int predecesora : actividad.getPredecesoras()) {
                if (!porNumero.containsKey(predecesora)) {
                    throw new IllegalArgumentException("La actividad " + actividad.getNumero()
                            + " referencia la predecesora inexistente " + predecesora + ".");
                }
                if (predecesora == actividad.getNumero()) {
                    throw new IllegalArgumentException("Una actividad no puede depender de sí misma: "
                            + actividad.getNumero() + ".");
                }
                if (!unicas.add(predecesora)) {
                    throw new IllegalArgumentException("Predecesora repetida " + predecesora
                            + " en la actividad " + actividad.getNumero() + ".");
                }
                sucesoras.get(predecesora).add(actividad.getNumero());
                gradosEntrada.merge(actividad.getNumero(), 1, Integer::sum);
            }
        }

        List<Integer> orden = ordenTopologico(porNumero, sucesoras, gradosEntrada);
        Map<Integer, Double> te = new HashMap<>();
        Map<Integer, Double> varianzas = new HashMap<>();
        Map<Integer, Double> es = new HashMap<>();
        Map<Integer, Double> ef = new HashMap<>();
        for (ActividadPert actividad : entrada) {
            double duracion = calculadorTE.calcular(actividad);
            double varianza = calculadorVarianza.calcular(actividad);
            if (!Double.isFinite(duracion) || !Double.isFinite(varianza)) {
                throw new IllegalArgumentException("Los tiempos de la actividad " + actividad.getNumero()
                        + " producen un resultado fuera del rango numérico.");
            }
            te.put(actividad.getNumero(), duracion);
            varianzas.put(actividad.getNumero(), varianza);
        }

        for (int id : orden) {
            ActividadPert actividad = porNumero.get(id);
            double inicio = 0.0;
            for (int predecesora : actividad.getPredecesoras()) {
                inicio = Math.max(inicio, ef.get(predecesora));
            }
            es.put(id, inicio);
            ef.put(id, inicio + te.get(id));
            if (!Double.isFinite(ef.get(id))) {
                throw new IllegalArgumentException("La duración total excede el rango numérico.");
            }
        }

        double duracionProyecto = 0.0;
        for (int id : porNumero.keySet()) {
            if (sucesoras.get(id).isEmpty()) duracionProyecto = Math.max(duracionProyecto, ef.get(id));
        }

        Map<Integer, Double> lf = new HashMap<>();
        Map<Integer, Double> ls = new HashMap<>();
        for (int i = orden.size() - 1; i >= 0; i--) {
            int id = orden.get(i);
            double finTardio = duracionProyecto;
            if (!sucesoras.get(id).isEmpty()) {
                finTardio = Double.POSITIVE_INFINITY;
                for (int sucesora : sucesoras.get(id)) {
                    finTardio = Math.min(finTardio, ls.get(sucesora));
                }
            }
            lf.put(id, finTardio);
            ls.put(id, finTardio - te.get(id));
        }

        Map<Integer, Double> holguras = new HashMap<>();
        for (int id : porNumero.keySet()) {
            double h = ls.get(id) - es.get(id);
            if (casiIgual(h, 0.0)) h = 0.0;
            if (h < 0.0) {
                throw new IllegalStateException("Se calculó una holgura negativa para la actividad " + id + ".");
            }
            holguras.put(id, h);
        }

        List<Integer> idsOrdenados = new ArrayList<>(porNumero.keySet());
        idsOrdenados.sort(Comparator.naturalOrder());
        List<ResultadoActividad> resultados = new ArrayList<>();
        int cantidadCriticas = 0;
        for (int id : idsOrdenados) {
            ActividadPert actividad = porNumero.get(id);
            boolean critica = casiIgual(holguras.get(id), 0.0);
            if (critica) cantidadCriticas++;
            resultados.add(new ResultadoActividad(actividad, te.get(id), varianzas.get(id),
                    es.get(id), ef.get(id), ls.get(id), lf.get(id), holguras.get(id), critica));
        }

        List<List<Integer>> rutasIds = new ArrayList<>();
        List<Integer> camino = new ArrayList<>();
        for (int id : idsOrdenados) {
            if (porNumero.get(id).getPredecesoras().isEmpty() && casiIgual(es.get(id), 0.0)
                    && casiIgual(holguras.get(id), 0.0)) {
                buscarRutas(id, porNumero, sucesoras, te, es, ef, holguras,
                        duracionProyecto, camino, rutasIds);
            }
        }
        if (rutasIds.isEmpty()) {
            throw new IllegalStateException("No se pudo reconstruir la ruta crítica de la red.");
        }

        List<ResultadoRuta> rutas = new ArrayList<>();
        double varianzaGlobal = 0.0;
        for (List<Integer> ruta : rutasIds) {
            double varianzaRuta = ruta.stream().mapToDouble(varianzas::get).sum();
            double desviacionRuta = Math.sqrt(varianzaRuta);
            double zRuta = desviacionRuta == 0.0 ? Double.NaN
                    : (tiempoMeta - duracionProyecto) / desviacionRuta;
            double probRuta = desviacionRuta == 0.0
                    ? (tiempoMeta >= duracionProyecto ? 1.0 : 0.0)
                    : DistribucionNormal.probabilidadAcumulada(zRuta);
            List<String> nombresRuta = ruta.stream().map(id -> porNumero.get(id).getNombre()).toList();
            rutas.add(new ResultadoRuta(ruta, nombresRuta, duracionProyecto, varianzaRuta,
                    desviacionRuta, zRuta, probRuta));
            varianzaGlobal = Math.max(varianzaGlobal, varianzaRuta);
        }

        double desviacion = Math.sqrt(varianzaGlobal);
        double z = desviacion == 0.0 ? Double.NaN : (tiempoMeta - duracionProyecto) / desviacion;
        double probabilidad = desviacion == 0.0
                ? (tiempoMeta >= duracionProyecto ? 1.0 : 0.0)
                : DistribucionNormal.probabilidadAcumulada(z);

        return new ResultadoProyecto(resultados, duracionProyecto, cantidadCriticas,
                rutas, tiempoMeta, varianzaGlobal, desviacion, z, probabilidad);
    }

    private List<Integer> ordenTopologico(Map<Integer, ActividadPert> porNumero,
            Map<Integer, List<Integer>> sucesoras, Map<Integer, Integer> gradosEntrada) {
        PriorityQueue<Integer> disponibles = new PriorityQueue<>();
        gradosEntrada.forEach((id, grado) -> { if (grado == 0) disponibles.add(id); });
        List<Integer> orden = new ArrayList<>();
        while (!disponibles.isEmpty()) {
            int id = disponibles.remove();
            orden.add(id);
            for (int sucesora : sucesoras.get(id)) {
                int nuevoGrado = gradosEntrada.merge(sucesora, -1, Integer::sum);
                if (nuevoGrado == 0) disponibles.add(sucesora);
            }
        }
        if (orden.size() != porNumero.size()) {
            throw new IllegalArgumentException("La red contiene un ciclo en las dependencias.");
        }
        return orden;
    }

    private void buscarRutas(int id, Map<Integer, ActividadPert> porNumero,
            Map<Integer, List<Integer>> sucesoras, Map<Integer, Double> te,
            Map<Integer, Double> es, Map<Integer, Double> ef, Map<Integer, Double> holguras,
            double duracionProyecto, List<Integer> camino, List<List<Integer>> rutas) {
        camino.add(id);
        if (sucesoras.get(id).isEmpty()) {
            if (casiIgual(ef.get(id), duracionProyecto)) rutas.add(List.copyOf(camino));
        } else {
            for (int siguiente : sucesoras.get(id)) {
                if (casiIgual(holguras.get(siguiente), 0.0)
                        && casiIgual(ef.get(id), es.get(siguiente))) {
                    buscarRutas(siguiente, porNumero, sucesoras, te, es, ef,
                            holguras, duracionProyecto, camino, rutas);
                }
            }
        }
        camino.remove(camino.size() - 1);
    }

    private boolean casiIgual(double a, double b) {
        return Math.abs(a - b) <= TOLERANCIA * Math.max(1.0, Math.max(Math.abs(a), Math.abs(b)));
    }
}
