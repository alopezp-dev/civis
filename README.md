# 🌍 CivisGeo - Sistema de Gestión Territorial

Un sistema interactivo para gestionar datos territoriales (países, divisiones, provincias y localidades) con importación desde CSV y almacenamiento en base de datos SQLite.

## ✨ Características Principales

- ✅ **Importación gradual desde CSV** - Carga país, divisiones, provincias y localidades por separado
- ✅ **Interfaz interactiva** - Menú amigable para usuarios
- ✅ **Consultas avanzadas** - Explora datos jerárquicamente
- ✅ **Estadísticas en tiempo real** - Visualiza resumen de la BD
- ✅ **Base de datos persistente** - SQLite para almacenamiento seguro
- ✅ **Seguridad** - Confirmación doble para operaciones destructivas

## 🏗️ Estructura del Proyecto

```
manualCivis/
├── src/
│   └── civisGeo/
│       ├── Main.java                 # Programa principal (Modo Texto)
│       ├── GuiMain.java              # Programa principal (Modo Gráfico)
│       ├── DBManager.java            # Gestor de base de datos
│       ├── EntidadTerritorial.java
│       ├── Pais.java
│       ├── ComunidadAutonoma.java
│       ├── CiudadAutonoma.java
│       ├── Provincia.java
│       └── Localidad.java
├── csv/
│   ├── paises.csv               # Lista de países
│   ├── divisiones.csv           # Divisiones territoriales
│   ├── provincias.csv           # Provincias
│   └── localidades.csv          # Localidades con población
├── lib/
│   └── sqlite-jdbc-3.51.1.0.jar # Driver SQLite
└── civis.db                      # Base de datos (generada automáticamente)
```

## 🚀 Inicio Rápido

### Compilación:
```bash
javac -d bin -cp "lib/sqlite-jdbc-3.51.1.0.jar;src" src/civisGeo/*.java
```

### Ejecución (Modo Texto):
```bash
java -cp "lib/sqlite-jdbc-3.51.1.0.jar;bin" civisGeo.Main
```

### Ejecución (Modo Gráfico):
```bash
java -cp "lib/sqlite-jdbc-3.51.1.0.jar;bin" civisGeo.GuiMain
```

## 📋 Opciones del Menú Principal

### 1️⃣ Importar datos desde CSV
Carga datos en la base de datos:
- **Opción 1**: Importar solo países
- **Opción 2**: Importar solo divisiones
- **Opción 3**: Importar solo provincias
- **Opción 4**: Importar solo localidades
- **Opción 5**: Importar todo en orden (recomendado)

### 2️⃣ Realizar consultas
Explora los datos almacenados:
- Ver todos los países
- Ver divisiones de un país específico
- Ver provincias de una división
- Ver localidades de una provincia
- Ver top 20 localidades por población

### 3️⃣ Mostrar estadísticas
Resumen de la base de datos:
- Total de países
- Total de divisiones
- Total de provincias
- Total de localidades
- Total de habitantes

### 4️⃣ Limpiar base de datos
Borrar toda la información (requiere confirmación)

### 5️⃣ Salir
Cierra el programa

## 📊 Formato de Archivos CSV

### paises.csv
```
Nombre
España
Portugal
Francia
```

### divisiones.csv
```
Nombre,Pais,Tipo
Andalucía,España,COMUNIDAD
Ceuta,España,CIUDAD
Lisboa,Portugal,COMUNIDAD
```

### provincias.csv
```
Nombre,Division,Pais
Sevilla,Andalucía,España
Barcelona,Cataluña,España
Lisboa,Lisboa,Portugal
```

### localidades.csv
```
Nombre,Habitantes,Provincia,Division,Pais
Sevilla,700000,Sevilla,Andalucía,España
Barcelona,1620000,Barcelona,Cataluña,España
Lisboa,505000,Lisboa,Lisboa,Portugal
```

## 💾 Base de Datos

Utiliza SQLite con 4 tablas relacionadas:

### Tabla: paises
- `nombre` (TEXT PRIMARY KEY)

### Tabla: divisiones
- `id` (INTEGER PRIMARY KEY)
- `nombre` (TEXT)
- `pais_nombre` (TEXT)
- `tipo` (TEXT) - COMUNIDAD o CIUDAD

### Tabla: provincias
- `id` (INTEGER PRIMARY KEY)
- `nombre` (TEXT)
- `division_nombre` (TEXT)
- `pais_nombre` (TEXT)

### Tabla: localidades
- `id` (INTEGER PRIMARY KEY)
- `nombre` (TEXT)
- `habitantes` (INTEGER)
- `provincia_nombre` (TEXT)
- `division_nombre` (TEXT)
- `pais_nombre` (TEXT)

## 🔒 Seguridad

- ✓ Confirmación simple para importaciones
- ✓ Confirmación doble (escribir "CONFIRMAR") para limpiar BD
- ✓ Validación de entradas del usuario
- ✓ Manejo de errores y excepciones

## 📊 Datos de Ejemplo Incluidos

El proyecto incluye datos de ejemplo para:
- 5 países (España, Portugal, Francia, Italia, Alemania)
- 23 divisiones territoriales
- 56 provincias
- 74 localidades principales

**Total de habitantes: 23.525.000**

## 🛠️ Requisitos

- Java 11 o superior
- SQLite-JDBC (incluido en `lib/`)
- Archivos CSV en la carpeta `csv/`

## 📝 Notas Importantes

1. **Importación jerárquica**: Debes importar en orden: Países → Divisiones → Provincias → Localidades
2. **Integridad referencial**: Cada entidad debe referenciar a padres válidos
3. **Persistencia**: Los datos se guardan automáticamente en `civis.db`
4. **Reutilización**: El programa utiliza la misma BD en cada ejecución

## 🎯 Ejemplos de Uso

### Importar data completa:
```
1. Seleccionar opción "1" (Importar datos)
2. Seleccionar opción "5" (Importar todo)
3. Confirmar con "s"
```

### Consultar localidades:
```
1. Seleccionar opción "2" (Consultas)
2. Seleccionar opción "4" (Localidades de provincia)
3. Escribir "Barcelona"
```

### Ver estadísticas:
```
1. Seleccionar opción "3" (Estadísticas)
```

## 🐛 Solución de Problemas

### "Archivo no encontrado"
- Verifica que los archivos CSV estén en la carpeta `csv/`
- Comprueba la ruta relativa desde donde ejecutas el programa

### "Base de datos vacía"
- Importa datos primero usando la opción 1 del menú
- Asegúrate de que los archivos CSV no estén vacíos

### "Sin divisiones encontradas"
- Verifica que el nombre del país sea exacto (mayúsculas)
- Importa los datos en orden: Países → Divisiones → Provincias

## 📄 Archivos Adicionales

- `GUIA_CSV.md` - Guía detallada de formats CSV
- `README_BD.md` - Documentación técnica de la base de datos

## 👨‍💻 Desarrollo

**Clases Java principales:**
- `Main.java` - Interfaz interactiva y menús
- `DBManager.java` - Operaciones de BD y importación CSV
- `Pais.java`, `ComunidadAutonoma.java`, `Provincia.java`, `Localidad.java` - Modelos de datos

## 📜 Licencia

Este proyecto es de código abierto y está disponible para uso educativo y comercial.

---

**¡Disfruta del sistema CivisGeo!** 🌍✨
# civis
# civis
# civis
