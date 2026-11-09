package com.equipo.matematicas;

import com.equipo.analisis.AnalisisPert;
import com.equipo.modelo.ActividadPert;
import com.equipo.reporte.ReporteResultados;
import com.equipo.resultados.ResultadoProyecto;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader entrada = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("============================================================");
        System.out.println("             SISTEMA DE ANÁLISIS PERT / CPM");
        System.out.println("============================================================");

        int cantidad = leerEntero(entrada, "Número de actividades: ", 1);
        List<ActividadPert> actividades = new ArrayList<>();
        for (int numero = 1; numero <= cantidad; numero++) {
            System.out.println("\nActividad " + numero);
            String nombre = leerTexto(entrada, "Nombre: ");
            double o = leerDouble(entrada, "Tiempo optimista (O): ", 0.0);
            double m = leerDouble(entrada, "Tiempo más probable (M): ", 0.0);
            double p = leerDouble(entrada, "Tiempo pesimista (P): ", 0.0);
            List<Integer> predecesoras = leerPredecesoras(entrada, numero);
            try {
                actividades.add(new ActividadPert(numero, nombre, o, m, p, predecesoras));
            } catch (IllegalArgumentException ex) {
                System.out.println("Datos inválidos: " + ex.getMessage());
                System.out.println("Vuelve a capturar esta actividad.");
                numero--;
            }
        }

        double meta = leerDouble(entrada, "\nTiempo meta del proyecto: ", 0.0);
        try {
            ResultadoProyecto resultado = new AnalisisPert().analizar(actividades, meta);
            new ReporteResultados().imprimir(resultado);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            System.out.println("No fue posible analizar el proyecto: " + ex.getMessage());
        }
    }

    private static String leerTexto(BufferedReader entrada, String mensaje) throws IOException {
        while (true) {
            System.out.print(mensaje);
            String linea = entrada.readLine();
            if (linea == null) throw new IOException("Fin de la entrada.");
            if (!linea.isBlank()) return linea.trim();
            System.out.println("El valor no puede estar vacío.");
        }
    }

    private static int leerEntero(BufferedReader entrada, String mensaje, int minimo) throws IOException {
        while (true) {
            System.out.print(mensaje);
            String linea = entrada.readLine();
            if (linea == null) throw new IOException("Fin de la entrada.");
            try {
                int valor = Integer.parseInt(linea.trim());
                if (valor >= minimo) return valor;
            } catch (NumberFormatException ignored) { }
            System.out.println("Ingresa un entero mayor o igual a " + minimo + ".");
        }
    }

    private static double leerDouble(BufferedReader entrada, String mensaje, double minimo)
            throws IOException {
        while (true) {
            System.out.print(mensaje);
            String linea = entrada.readLine();
            if (linea == null) throw new IOException("Fin de la entrada.");
            try {
                double valor = Double.parseDouble(linea.trim().replace(',', '.'));
                if (Double.isFinite(valor) && valor >= minimo) return valor;
            } catch (NumberFormatException ignored) { }
            System.out.println("Ingresa un número finito mayor o igual a " + minimo + ".");
        }
    }

    private static List<Integer> leerPredecesoras(BufferedReader entrada, int numero)
            throws IOException {
        while (true) {
            System.out.print("Predecesoras (números separados por coma; 0 si ninguna): ");
            String linea = entrada.readLine();
            if (linea == null) throw new IOException("Fin de la entrada.");
            String limpio = linea.trim();
            if (limpio.isEmpty() || limpio.equalsIgnoreCase("ninguna")
                    || limpio.equals("-") || limpio.equals("0")) return List.of();
            try {
                List<Integer> ids = new ArrayList<>();
                for (String parte : limpio.split("[,\\s]+")) {
                    int id = Integer.parseInt(parte);
                    if (id <= 0) throw new IllegalArgumentException("Los números de actividad deben ser positivos.");
                    if (ids.contains(id)) throw new IllegalArgumentException("No repitas predecesoras.");
                    ids.add(id);
                }
                return List.copyOf(ids);
            } catch (IllegalArgumentException ex) {
                System.out.println("Entrada inválida: " + ex.getMessage());
            }
        }
    }
}
