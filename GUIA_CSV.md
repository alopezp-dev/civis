# Estructura de Archivos CSV para CivisGeo

## 📁 Archivos CSV Disponibles

### 1. **csv/paises.csv**
Contiene la lista de países disponibles.
- Columnas: `Nombre`
- Ejemplo:
  ```
  Nombre
  España
  Portugal
  Francia
  ```

### 2. **csv/divisiones.csv**
Contiene divisiones territoriales (Comunidades Autónomas, Ciudades Autónomas, etc.)
- Columnas: `Nombre, Pais, Tipo`
- Tipo puede ser: `COMUNIDAD` o `CIUDAD`
- Ejemplo:
  ```
  Nombre,Pais,Tipo
  Andalucía,España,COMUNIDAD
  Ceuta,España,CIUDAD
  ```

### 3. **csv/provincias.csv**
Contiene provincias y sus divisiones asociadas.
- Columnas: `Nombre, Division, Pais`
- Ejemplo:
  ```
  Nombre,Division,Pais
  Sevilla,Andalucía,España
  Barcelona,Cataluña,España
  ```

### 4. **csv/localidades.csv**
Contiene localidades (ciudades, pueblos, etc.) con población.
- Columnas: `Nombre, Habitantes, Provincia, Division, Pais`
- Ejemplo:
  ```
  Nombre,Habitantes,Provincia,Division,Pais
  Sevilla,700000,Sevilla,Andalucía,España
  Barcelona,1620000,Barcelona,Cataluña,España
  ```

## ⚙️ Compilación y Ejecución

### Compilar el proyecto:
```bash
javac -d . -cp "lib/sqlite-jdbc-3.51.1.0.jar;src" src/civisGeo/*.java src/Main.java
```

### Ejecutar la aplicación:
```bash
java -cp "lib/sqlite-jdbc-3.51.1.0.jar;." Main
```

## 🎯 Funcionalidades

### Menú Principal
1. **Importar datos desde CSV** - Carga datos de forma gradual
   - Países
   - Divisiones
   - Provincias
   - Localidades
   - O importar todo en orden (recomendado)

2. **Realizar consultas** - Busca información en la BD
   - Listar todos los países
   - Ver divisiones de un país
   - Ver provincias de una división
   - Ver localidades de una provincia
   - Ver top 20 localidades por población

3. **Mostrar estadísticas** - Resumen de la BD
   - Total de países
   - Total de divisiones
   - Total de provincias
   - Total de localidades
   - Total de habitantes

4. **Limpiar base de datos** - Borrar toda la información (requiere confirmación de seguridad)

## 📊 Base de Datos

La aplicación utiliza SQLite (`civis.db`) con 4 tablas:
- `paises` - Almacena países
- `divisiones` - Almacena divisiones (Comunidades/Ciudades Autónomas)
- `provincias` - Almacena provincias
- `localidades` - Almacena localidades con población

## 💡 Ejemplo de Uso

1. Ejecutar: `java -cp "lib/sqlite-jdbc-3.51.1.0.jar;." Main`
2. Seleccionar opción 1 (Importar datos)
3. Seleccionar opción 5 (Importar todo)
4. Confirmar con 's'
5. Ver estadísticas con opción 3
6. Hacer consultas con opción 2
7. Salir con opción 5

## 📝 Notas Importantes

- Los datos se guardan en `civis.db` de forma persistente
- Se deben importar en orden: Países → Divisiones → Provincias → Localidades
- Cada localidad debe tener una provincia válida
- Cada provincia debe tener una división válida
- Cada división debe tener un país válido

¡Disfruta del sistema CivisGeo! 🌍
