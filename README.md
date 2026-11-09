# Sistema de análisis PERT/CPM

Aplicación de consola en Java 17 para estimar duraciones PERT, analizar una red de precedencias con CPM y calcular una probabilidad aproximada de cumplimiento de plazo.

## Ejecución

Desde la raíz del proyecto, compila y ejecuta:

```text
mvn compile
java -cp target/classes com.equipo.matematicas.Main
```

También se puede ejecutar `com.equipo.matematicas.Main` desde el IDE después de compilar.

Registra cada actividad con nombre, O, M y P, y luego sus predecesoras por número. Usa `0` cuando no tenga predecesoras. Los identificadores se asignan en el orden de captura. Por ejemplo, para una actividad que depende de las actividades 1 y 3, escribe `1,3`. Se aceptan dependencias a actividades capturadas después; el programa valida que existan y que la red no tenga ciclos.

## Organización

- `modelo.ActividadPert`: estimaciones de una actividad y sus predecesoras.
- `calculos.TiempoEsperado` y `calculos.Varianza`: fórmulas PERT por actividad.
- `analisis.AnalisisPert`: validación de la red y recorridos CPM.
- `analisis.DistribucionNormal`: aproximación de la distribución normal estándar.
- `resultados`: estructuras inmutables con resultados por actividad, ruta y proyecto.
- `reporte.ReporteResultados`: tabla y resumen en consola.
- `matematicas.Main`: captura de datos y coordinación.

## Fórmulas y supuestos

- `TE = (O + 4M + P) / 6`
- `Varianza = ((P - O) / 6)^2`
- `ES = max(EF de predecesoras)`; si no hay predecesoras, `ES = 0`.
- `EF = ES + TE`.
- `LF = min(LS de sucesoras)`; para actividades terminales se usa la duración del proyecto.
- `LS = LF - TE`; `H = LS - ES`.
- Una actividad es crítica si su holgura está a una tolerancia numérica de cero.
- La duración del proyecto es el mayor EF de las actividades terminales.
- La varianza de una ruta crítica es la suma de las varianzas de sus actividades, bajo independencia de duraciones. Se muestran todas las rutas críticas y sus varianzas.
- Si hay varias rutas críticas, el resumen global usa la mayor varianza de esas rutas como aproximación. El proyecto es el máximo de las duraciones de las rutas y su distribución exacta requiere un análisis más avanzado; por ello, la probabilidad reportada es una aproximación normal PERT, no una probabilidad exacta.
- Si la varianza es cero, la duración se trata como determinista y Z no está definido; la probabilidad es 0% si la meta es menor que la duración y 100% si la alcanza o supera.
