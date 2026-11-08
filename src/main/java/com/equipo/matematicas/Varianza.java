package com.equipo.matematicas;

public class Varianza {
    public double calcular(ActividadPert a){
        return Math.pow((a.P - a.O) / 6, 2);
    }
}
