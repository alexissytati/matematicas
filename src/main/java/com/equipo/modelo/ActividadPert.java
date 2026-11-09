package com.equipo.modelo;

import java.util.List;

/** Datos de una actividad y los identificadores de sus predecesoras. */
public class ActividadPert {

    private final int numero;
    private final String nombre;
    private final double optimista;
    private final double masProbable;
    private final double pesimista;
    private final List<Integer> predecesoras;

    /** Constructor conservado para calcular TE y varianza de una actividad aislada. */
    public ActividadPert(String nombre, double optimista,
                         double masProbable, double pesimista) {
        this(0, nombre, optimista, masProbable, pesimista, List.of());
    }

    public ActividadPert(int numero, String nombre, double optimista,
                         double masProbable, double pesimista,
                         List<Integer> predecesoras) {
        if (numero < 0) {
            throw new IllegalArgumentException("El número de actividad no puede ser negativo.");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío.");
        }
        if (!Double.isFinite(optimista) || !Double.isFinite(masProbable)
                || !Double.isFinite(pesimista)) {
            throw new IllegalArgumentException("Los tiempos deben ser números finitos.");
        }
        if (optimista < 0 || masProbable < 0 || pesimista < 0) {
            throw new IllegalArgumentException("Los tiempos no pueden ser negativos.");
        }
        if (optimista > masProbable || masProbable > pesimista) {
            throw new IllegalArgumentException("Debe cumplirse: O <= M <= P.");
        }
        if (predecesoras == null) {
            throw new IllegalArgumentException("La lista de predecesoras no puede ser nula.");
        }

        this.numero = numero;
        this.nombre = nombre.trim();
        this.optimista = optimista;
        this.masProbable = masProbable;
        this.pesimista = pesimista;
        this.predecesoras = List.copyOf(predecesoras);
    }

    public int getNumero() { return numero; }
    public String getNombre() { return nombre; }
    public double getOptimista() { return optimista; }
    public double getMasProbable() { return masProbable; }
    public double getPesimista() { return pesimista; }
    public List<Integer> getPredecesoras() { return predecesoras; }
}
