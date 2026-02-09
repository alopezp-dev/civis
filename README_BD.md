# Sistema de Base de Datos para Civis

Este proyecto implementa una base de datos SQLite para gestionar entidades territoriales (Países, Divisiones, Provincias y Localidades) con soporte para importación desde CSV.

## Estructura de la Base de Datos

### Tablas:

1. **paises**
   - nombre (TEXT PRIMARY KEY)

2. **divisiones** (Comunidades o Ciudades Autónomas)
   - id (INTEGER PRIMARY KEY)
   - nombre (TEXT)
   - pais_nombre (TEXT)
   - tipo (TEXT) - "COMUNIDAD" o "CIUDAD"

3. **provincias**
   - id (INTEGER PRIMARY KEY)
   - nombre (TEXT)
   - division_nombre (TEXT)
   - pais_nombre (TEXT)

4. **localidades**
   - id (INTEGER PRIMARY KEY)
   - nombre (TEXT)
   - habitantes (INTEGER)
   - provincia_nombre (TEXT)
   - division_nombre (TEXT)
   - pais_nombre (TEXT)

## Uso de la Base de Datos

### Compilación:
```bash
javac -d . -cp "lib/sqlite-jdbc-3.51.1.0.jar;src" src/civisGeo/*.java src/Main.java
```

### Ejecución:
```bash
java -cp "lib/sqlite-jdbc-3.51.1.0.jar;." Main
```

## Importación desde CSV

Crear un archivo CSV con la siguiente estructura:

```
Pais,Division,TipoDivision,Provincia,Localidad,Habitantes
España,Andalucía,COMUNIDAD,Sevilla,Sevilla,700000
España,Cataluña,COMUNIDAD,Barcelona,Barcelona,1620000
```

### Cómo usar:
```java
DBManager db = new DBManager();
db.importarDesdeCSV("datos.csv");
```

## API del DBManager

### Guardar datos:
```java
// Guardar un país
db.guardarPais("España");

// Guardar una división (comunidad/ciudad autónoma)
db.guardarDivision("Andalucía", "España", "COMUNIDAD");

// Guardar una provincia
db.guardarProvincia("Sevilla", "Andalucía", "España");

// Guardar una localidad
db.guardarLocalidad("Sevilla", 700000, "Sevilla", "Andalucía", "España");
```

### Obtener datos:
```java
// Obtener todos los países
List<String> paises = db.obtenerPaises();

// Obtener todas las localidades
List<String> localidades = db.obtenerLocalidades();
```

### Otras operaciones:
```java
// Limpiar la base de datos
db.limpiarBaseDatos();

// Importar desde CSV
db.importarDesdeCSV("archivo.csv");
```

## Archivo de base de datos

El archivo `civis.db` se genera automáticamente en el directorio raíz del proyecto cuando se ejecuta por primera vez.

## Ejemplo completo

Consulta `Main.java` para ver un ejemplo completo de uso que:
1. Inicializa la base de datos
2. Importa datos desde CSV
3. Consulta los datos guardados
4. Añade datos adicionales manualmente
