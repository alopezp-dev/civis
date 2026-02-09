
import java.io.*;
import java.sql.*;
import java.util.*;
import civisGeo.*;

public class DBManager {
    private static final String URL = "jdbc:sqlite:civis.db";

    public DBManager() {
        initDatabase();
    }

    // Crea las tablas si el archivo .db es nuevo
    private void initDatabase() {
        try (Connection conn = DriverManager.getConnection(URL);
                Statement stmt = conn.createStatement()) {

            // Tabla de Países
            stmt.execute("CREATE TABLE IF NOT EXISTS paises (nombre TEXT PRIMARY KEY)");

            // Tabla de Divisiones
            stmt.execute("CREATE TABLE IF NOT EXISTS divisiones (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "nombre TEXT NOT NULL, " +
                    "pais_nombre TEXT NOT NULL, " +
                    "tipo TEXT NOT NULL, " +
                    "UNIQUE(nombre, pais_nombre), " +
                    "FOREIGN KEY(pais_nombre) REFERENCES paises(nombre))");

            // Tabla de Provincias
            stmt.execute("CREATE TABLE IF NOT EXISTS provincias (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "nombre TEXT NOT NULL, " +
                    "division_nombre TEXT NOT NULL, " +
                    "pais_nombre TEXT NOT NULL, " +
                    "UNIQUE(nombre, division_nombre), " +
                    "FOREIGN KEY(division_nombre) REFERENCES divisiones(nombre), " +
                    "FOREIGN KEY(pais_nombre) REFERENCES paises(nombre))");

            // Tabla de Localidades
            stmt.execute("CREATE TABLE IF NOT EXISTS localidades (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "nombre TEXT NOT NULL, " +
                    "habitantes INTEGER DEFAULT 0, " +
                    "provincia_nombre TEXT, " +
                    "division_nombre TEXT, " +
                    "pais_nombre TEXT NOT NULL, " +
                    "codigo_postal TEXT, " +
                    "capital_tipo TEXT, " +
                    "UNIQUE(nombre, provincia_nombre, pais_nombre), " +
                    "FOREIGN KEY(provincia_nombre) REFERENCES provincias(nombre), " +
                    "FOREIGN KEY(division_nombre) REFERENCES divisiones(nombre), " +
                    "FOREIGN KEY(pais_nombre) REFERENCES paises(nombre))");

            // Intento de añadir la columna si no existe (para bases de datos ya creadas)
            try {
                stmt.execute("ALTER TABLE localidades ADD COLUMN capital_tipo TEXT");
            } catch (SQLException ignored) {
            }

            // --- NUEVAS TABLAS PARA CENSO (CALLES CON EDIFICIOS/NÚMEROS) ---

            // Tabla Calle_Edificios: almacena calle + número como registros individuales
            // Nombre de calle + número de edificio combinados para facilitar búsquedas
            stmt.execute("CREATE TABLE IF NOT EXISTS calle_edificios (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "cod_via TEXT, " +
                    "calle TEXT NOT NULL, " + // Nombre de la calle (e.g., "Calle Mayor")
                    "edificio INTEGER NOT NULL, " + // Número del edificio (e.g., 42)
                    "tipo_via TEXT, " + // calle, avenida, plaza, etc.
                    "localidad_nombre TEXT, " +
                    "provincia_nombre TEXT, " + // Necesario para desambiguar localidad
                    "FOREIGN KEY(localidad_nombre, provincia_nombre) REFERENCES localidades(nombre, provincia_nombre))");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Importa datos del censo (calles con números de edificios) desde una base de
     * datos SQLite
     * externa.
     * 
     * @param censusDbPath Ruta al archivo census.db
     */
    public void importarDatosCenso(String censusDbPath) {
        File dbFile = new File(censusDbPath);
        if (!dbFile.exists()) {
            System.out.println("❌ ERROR: El archivo " + dbFile.getAbsolutePath() + " no existe.");
            return;
        }

        String attachSql = "ATTACH DATABASE ? AS census";
        String detachSql = "DETACH DATABASE census";

        // Mapeo: Normalized Key -> [RealName, RealProvince]
        // Key format: MUNICIPO|PROVINCIA (Upper/Trimmed)
        Map<String, String[]> localidadMap = new HashMap<>();

        try (Connection conn = DriverManager.getConnection(URL)) {
            // OPTIMIZACIONES DE VELOCIDAD
            Statement pragmaStmt = conn.createStatement();
            pragmaStmt.execute("PRAGMA foreign_keys = OFF"); // Desactivar FKs durante importación masiva
            pragmaStmt.execute("PRAGMA synchronous = OFF"); // Escritura asíncrona (riesgo si se va la luz, pero muy
                                                            // rápido)
            pragmaStmt.execute("PRAGMA journal_mode = MEMORY"); // Journal en RAM
            pragmaStmt.close();

            conn.setAutoCommit(false);

            // 1. Cargar cache de Localidades
            System.out.println("Cargando caché de localidades para optimizar...");
            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT nombre, provincia_nombre FROM localidades")) {
                while (rs.next()) {
                    String nombre = rs.getString("nombre");
                    String provincia = rs.getString("provincia_nombre");

                    if (nombre != null) {
                        String cleanNombre = nombre.trim().toUpperCase();
                        String cleanProvincia = (provincia != null) ? provincia.trim().toUpperCase() : null;

                        // Generar claves para el mapa
                        // 1. Clave exacta
                        addMapEntry(localidadMap, cleanNombre, cleanProvincia, nombre, provincia);

                        // 2. Claves alternativas dividiendo por "/" o "-" (ej: "Alicante/Alacant")
                        // Esto aumenta el 'hit rate' si el censo usa solo uno de los nombres
                        if (cleanNombre.contains("/") || cleanNombre.contains("-")) {
                            String[] parts = cleanNombre.split("[/-]");
                            for (String part : parts) {
                                if (!part.trim().isEmpty()) {
                                    addMapEntry(localidadMap, part.trim(), cleanProvincia, nombre, provincia);
                                }
                            }
                        }
                    }
                }
            }
            System.out.println("Localidades cacheadas (incl. alias): " + localidadMap.size());

            // 2. Attach census DB
            try (PreparedStatement pstmt = conn.prepareStatement(attachSql)) {
                pstmt.setString(1, dbFile.getAbsolutePath());
                pstmt.execute();
            }
            System.out.println("Base de datos census.db adjuntada.");

            // 3. Importación masiva
            String selectSql = "SELECT nombre_via, numero, tipo_vial, municipio, provincia FROM census.direcciones";
            String insertSql = "INSERT INTO calle_edificios (cod_via, calle, edificio, tipo_via, localidad_nombre, provincia_nombre) VALUES (?, ?, ?, ?, ?, ?)";

            int insertedCount = 0;
            int batchSize = 0;
            final int BATCH_LIMIT = 50000; // Lote más grande para velocidad

            try (Statement selectStmt = conn.createStatement();
                    PreparedStatement insertPstmt = conn.prepareStatement(insertSql)) {

                System.out.println("Leyendo y procesando direcciones del censo...");
                try (ResultSet rs = selectStmt.executeQuery(selectSql)) {
                    while (rs.next()) {
                        String mun = rs.getString("municipio");
                        String prov = rs.getString("provincia");

                        if (mun == null)
                            continue;

                        String lookupKey = mun.trim().toUpperCase();
                        if (prov != null) {
                            lookupKey += "|" + prov.trim().toUpperCase();
                        }

                        String[] match = localidadMap.get(lookupKey);

                        // Intento de fallback: si no cuadra la provincia (o en censo el formato es
                        // distinto),
                        // intentar buscar solo por municipio si es único?
                        // Por seguridad, requerimos coincidencia de provincia si está disponible.
                        // Pero si en el mapa tenemos entradas con provincia, la key debe incluirla.
                        // La función 'addMapEntry' añade keys CON provincia.

                        if (match != null) {
                            String calle = rs.getString("nombre_via");
                            String tipoVia = rs.getString("tipo_vial");
                            // Optimización: parseo rápido de int o catch (costoso en excepciones masivas)
                            // La mayoría son números.
                            String numStr = rs.getString("numero");
                            int num = 0;
                            if (numStr != null && !numStr.isEmpty()) {
                                try {
                                    num = Integer.parseInt(numStr);
                                } catch (NumberFormatException ignored) {
                                }
                            }

                            insertPstmt.setString(1, null);
                            insertPstmt.setString(2, calle);
                            insertPstmt.setInt(3, num);
                            insertPstmt.setString(4, tipoVia);
                            insertPstmt.setString(5, match[0]); // Nombre real localidad
                            insertPstmt.setString(6, match[1]); // Nombre real provincia
                            insertPstmt.addBatch();

                            batchSize++;
                            insertedCount++;

                            if (batchSize >= BATCH_LIMIT) {
                                insertPstmt.executeBatch();
                                conn.commit();
                                batchSize = 0;
                                System.out.print("."); // Progreso visual
                                if (insertedCount % 1000000 == 0)
                                    System.out.print(" " + (insertedCount / 1000000) + "M ");
                            }
                        }
                    }
                }
                if (batchSize > 0) {
                    insertPstmt.executeBatch();
                    conn.commit();
                }
            }

            System.out.println("\nProceso finalizado. Total calles importadas: " + insertedCount);

            // Detach
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(detachSql);
            }

            // Restaurar configuración segura (aunque al cerrar conexión se resetea)
            pragmaStmt = conn.createStatement();
            pragmaStmt.execute("PRAGMA foreign_keys = ON");
            pragmaStmt.execute("PRAGMA synchronous = NORMAL");
            pragmaStmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addMapEntry(Map<String, String[]> map, String mun, String prov, String realMun, String realProv) {
        // Añadir clave con provincia
        if (prov != null) {
            map.put(mun + "|" + prov, new String[] { realMun, realProv });
        }
        // Añadir clave SIN provincia (para casos donde censo tenga provincia nula o
        // diferente formato,
        // pero asumimos riesgo de colisión. Si hay colisión, map.put sobrescribe,
        // tomamos el último. Recomendable evitar si posible).
        // Mejor NO añadir sin provincia para evitar mezclar pueblos con mismo nombre.
        // Solo si 'prov' es null en origen (localidades main)
        else {
            map.put(mun, new String[] { realMun, realProv });
        }
    }

    private String[] parseCsvLine(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString().trim());
        return tokens.toArray(new String[0]);
    }

    /**
     * Importa países desde un archivo CSV.
     * Formato esperado: Nombre
     * 
     * @param rutaArchivo Ruta al archivo CSV
     * @return Número de registros importados
     */
    public int importarPaisesCSV(String rutaArchivo) {
        int contador = 0;
        String sql = "INSERT OR REPLACE INTO paises (nombre) VALUES (?)";
        try (Connection conn = DriverManager.getConnection(URL);
                BufferedReader br = new BufferedReader(new FileReader(rutaArchivo));
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            String linea;
            br.readLine(); // Skip header
            while ((linea = br.readLine()) != null) {
                String nombre = linea.trim();
                if (nombre.startsWith("\"") && nombre.endsWith("\"")) {
                    nombre = nombre.substring(1, nombre.length() - 1);
                }
                if (!nombre.isEmpty()) {
                    pstmt.setString(1, nombre);
                    pstmt.addBatch();
                    contador++;
                }
            }
            pstmt.executeBatch();
            conn.commit();
            System.out.println("Importación de países completada: " + contador + " registros.");
        } catch (IOException | SQLException e) {
            System.out.println("Error al importar países: " + e.getMessage());
        }
        return contador;
    }

    /**
     * Importa divisiones desde un archivo CSV.
     * Formato esperado: Nombre, Pais, Tipo
     * 
     * @param rutaArchivo Ruta al archivo CSV
     * @return Número de registros importados
     */
    public int importarDivisionesCSV(String rutaArchivo) {
        int contador = 0;
        String sql = "INSERT OR REPLACE INTO divisiones (nombre, pais_nombre, tipo) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL);
                BufferedReader br = new BufferedReader(new FileReader(rutaArchivo));
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            String linea;
            br.readLine(); // Skip header
            while ((linea = br.readLine()) != null) {
                String[] partes = parseCsvLine(linea);
                if (partes.length >= 3) {
                    if (!partes[0].isEmpty() && !partes[1].isEmpty()) {
                        pstmt.setString(1, partes[0]);
                        pstmt.setString(2, partes[1]);
                        pstmt.setString(3, partes[2]);
                        pstmt.addBatch();
                        contador++;
                    }
                }
            }
            pstmt.executeBatch();
            conn.commit();
            System.out.println("Importación de divisiones completada: " + contador + " registros.");
        } catch (IOException | SQLException e) {
            System.out.println("Error al importar divisiones: " + e.getMessage());
        }
        return contador;
    }

    /**
     * Importa provincias desde un archivo CSV.
     * Formato esperado: Nombre, Division, Pais
     * 
     * @param rutaArchivo Ruta al archivo CSV
     * @return Número de registros importados
     */
    public int importarProvinciasCSV(String rutaArchivo) {
        int contador = 0;
        String sql = "INSERT OR REPLACE INTO provincias (nombre, division_nombre, pais_nombre) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL);
                BufferedReader br = new BufferedReader(new FileReader(rutaArchivo));
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            String linea;
            br.readLine(); // Skip header
            while ((linea = br.readLine()) != null) {
                String[] partes = parseCsvLine(linea);
                if (partes.length >= 3) {
                    if (!partes[0].isEmpty() && !partes[1].isEmpty()) {
                        pstmt.setString(1, partes[0]);
                        pstmt.setString(2, partes[1]);
                        pstmt.setString(3, partes[2]);
                        pstmt.addBatch();
                        contador++;
                    }
                }
            }
            pstmt.executeBatch();
            conn.commit();
            System.out.println("Importación de provincias completada: " + contador + " registros.");
        } catch (IOException | SQLException e) {
            System.out.println("Error al importar provincias: " + e.getMessage());
        }
        return contador;
    }

    /**
     * Importa localidades desde un archivo CSV.
     * 
     * @param rutaArchivo Ruta al archivo CSV
     * @return Número de registros importados
     */
    public int importarLocalidadesCSV(String rutaArchivo) {
        int contador = 0;

        String sql = "INSERT INTO localidades (nombre, habitantes, codigo_postal, provincia_nombre, division_nombre, pais_nombre) VALUES (?, ?, ?, ?, ?, ?) "
                +
                "ON CONFLICT(nombre, provincia_nombre, pais_nombre) DO UPDATE SET " +
                "habitantes = excluded.habitantes, " +
                "codigo_postal = excluded.codigo_postal, " +
                "division_nombre = excluded.division_nombre, " +
                "pais_nombre = excluded.pais_nombre";

        try (Connection conn = DriverManager.getConnection(URL);
                BufferedReader br = new BufferedReader(new FileReader(rutaArchivo));
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            String linea;
            br.readLine(); // Skip header
            while ((linea = br.readLine()) != null) {
                String[] partes = parseCsvLine(linea);

                if (partes.length >= 5) {
                    String nombre = partes[0];
                    if (nombre.isEmpty())
                        continue;

                    int habitantes = 0;
                    try {
                        habitantes = Integer.parseInt(partes[1]);
                    } catch (NumberFormatException e) {
                    }

                    String codigo = null;
                    String provincia, division, pais;

                    if (partes.length >= 6) {
                        codigo = partes[2];
                        provincia = partes[3];
                        division = partes[4];
                        pais = partes[5];
                    } else {
                        provincia = partes[2];
                        division = partes[3];
                        pais = partes[4];
                    }

                    pstmt.setString(1, nombre);
                    pstmt.setInt(2, habitantes);
                    pstmt.setString(3, codigo);
                    pstmt.setString(4, provincia);
                    pstmt.setString(5, division);
                    pstmt.setString(6, pais);
                    pstmt.addBatch();
                    contador++;
                }
            }
            pstmt.executeBatch();
            conn.commit();
            System.out.println("Importación de localidades completada: " + contador + " registros.");
        } catch (IOException | SQLException e) {
            System.out.println("Error al importar localidades: " + e.getMessage());
        }
        return contador;
    }

    /**
     * Importa capitales desde un archivo CSV.
     * Actualiza la columna capital_tipo de las localidades existentes.
     * Formato: Nombre, Provincia, Division, Tipo
     * 
     * @param rutaArchivo Ruta al archivo CSV
     * @return Número de registros actualizados
     */
    public int importarCapitalesCSV(String rutaArchivo) {
        int contador = 0;
        String sql = "UPDATE localidades SET capital_tipo = ? WHERE nombre = ? AND provincia_nombre = ?";
        try (Connection conn = DriverManager.getConnection(URL);
                BufferedReader br = new BufferedReader(new FileReader(rutaArchivo));
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            String linea;
            br.readLine(); // Skip header
            while ((linea = br.readLine()) != null) {
                String[] partes = parseCsvLine(linea);
                if (partes.length >= 4) {
                    String nombre = partes[0];
                    String provincia = partes[1];
                    String tipo = partes[3];

                    if (!nombre.isEmpty()) {
                        pstmt.setString(1, tipo);
                        pstmt.setString(2, nombre);
                        pstmt.setString(3, provincia);
                        pstmt.addBatch();
                        contador++;
                    }
                }
            }
            int[] updates = pstmt.executeBatch();
            conn.commit();
            System.out.println("Procesamiento de capitales completado. Registros procesados: " + contador);
        } catch (IOException | SQLException e) {
            System.out.println("Error al importar capitales: " + e.getMessage());
        }
        return contador;
    }

    public List<String> obtenerPaises() {
        List<String> paises = new ArrayList<>();
        String sql = "SELECT nombre FROM paises ORDER BY nombre";
        try (Connection conn = DriverManager.getConnection(URL);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                paises.add(rs.getString("nombre"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return paises;
    }

    public List<String[]> obtenerDivisiones(String pais) {
        List<String[]> divisiones = new ArrayList<>();
        String sql = "SELECT nombre, tipo, pais_nombre FROM divisiones WHERE pais_nombre = ? ORDER BY nombre";
        try (Connection conn = DriverManager.getConnection(URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, pais);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                divisiones.add(new String[] {
                        rs.getString("nombre"),
                        rs.getString("tipo"),
                        rs.getString("pais_nombre")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return divisiones;
    }

    public List<String[]> obtenerProvincias(String division) {
        List<String[]> provincias = new ArrayList<>();
        String sql = "SELECT nombre, division_nombre, pais_nombre FROM provincias WHERE division_nombre = ? ORDER BY nombre";
        try (Connection conn = DriverManager.getConnection(URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, division);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                provincias.add(new String[] {
                        rs.getString("nombre"),
                        rs.getString("division_nombre"),
                        rs.getString("pais_nombre")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return provincias;
    }

    /**
     * Obtiene localidades de una provincia, ordenadas alfabéticamente.
     * 
     * @param provincia Nombre de la provincia
     * @return Lista de arrays [nombre, habitantes, provincia, cp, capital_tipo]
     */
    public List<String[]> obtenerLocalidadesDeProvincia(String provincia) {
        List<String[]> localidades = new ArrayList<>();
        String sql = "SELECT nombre, habitantes, provincia_nombre, codigo_postal, capital_tipo FROM localidades WHERE provincia_nombre = ? ORDER BY nombre ASC";
        try (Connection conn = DriverManager.getConnection(URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, provincia);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String capital = rs.getString("capital_tipo");
                localidades.add(new String[] {
                        rs.getString("nombre"),
                        String.valueOf(rs.getInt("habitantes")),
                        rs.getString("provincia_nombre"),
                        rs.getString("codigo_postal") != null ? rs.getString("codigo_postal") : "",
                        capital != null ? capital : ""
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return localidades;
    }

    /**
     * Obtiene el top 20 de localidades por habitantes.
     * 
     * @return Lista de arrays [nombre, habitantes, provincia, cp, capital_tipo]
     */
    public List<String[]> obtenerLocalidades() {
        List<String[]> localidades = new ArrayList<>();
        String sql = "SELECT nombre, habitantes, provincia_nombre, codigo_postal, capital_tipo FROM localidades ORDER BY habitantes DESC LIMIT 20";
        try (Connection conn = DriverManager.getConnection(URL);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String capital = rs.getString("capital_tipo");
                localidades.add(new String[] {
                        rs.getString("nombre"),
                        String.valueOf(rs.getInt("habitantes")),
                        rs.getString("provincia_nombre"),
                        rs.getString("codigo_postal") != null ? rs.getString("codigo_postal") : "",
                        capital != null ? capital : ""
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return localidades;
    }

    /**
     * Busca localidades por nombre (búsqueda parcial), ordenadas alfabéticamente.
     * 
     * @param busqueda Texto a buscar
     * @return Lista de arrays [nombre, habitantes, provincia, cp, capital_tipo]
     */
    public List<String[]> buscarLocalidades(String busqueda) {
        List<String[]> localidades = new ArrayList<>();
        String sql = "SELECT nombre, habitantes, provincia_nombre, codigo_postal, capital_tipo FROM localidades WHERE nombre LIKE ? ORDER BY nombre ASC LIMIT 100";
        try (Connection conn = DriverManager.getConnection(URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + busqueda + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String capital = rs.getString("capital_tipo");
                localidades.add(new String[] {
                        rs.getString("nombre"),
                        String.valueOf(rs.getInt("habitantes")),
                        rs.getString("provincia_nombre"),
                        rs.getString("codigo_postal") != null ? rs.getString("codigo_postal") : "",
                        capital != null ? capital : ""
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return localidades;
    }

    public Map<String, Integer> obtenerEstadisticasMap() {
        Map<String, Integer> stats = new HashMap<>();
        try (Connection conn = DriverManager.getConnection(URL);
                Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as total FROM paises");
            if (rs.next())
                stats.put("Paises", rs.getInt("total"));

            rs = stmt.executeQuery("SELECT COUNT(*) as total FROM divisiones");
            if (rs.next())
                stats.put("Divisiones", rs.getInt("total"));

            rs = stmt.executeQuery("SELECT COUNT(*) as total FROM provincias");
            if (rs.next())
                stats.put("Provincias", rs.getInt("total"));

            rs = stmt.executeQuery("SELECT COUNT(*) as total FROM localidades");
            if (rs.next())
                stats.put("Localidades", rs.getInt("total"));

            rs = stmt.executeQuery("SELECT SUM(habitantes) as total FROM localidades");
            if (rs.next())
                stats.put("Habitantes", rs.getInt("total"));

            try {
                rs = stmt.executeQuery("SELECT COUNT(*) as total FROM localidades WHERE capital_tipo IS NOT NULL");
                if (rs.next())
                    stats.put("Capitales", rs.getInt("total"));
            } catch (SQLException e) {
            }

            // Stats Censo
            try {
                rs = stmt.executeQuery("SELECT COUNT(*) as total FROM calle_edificios");
                if (rs.next())
                    stats.put("Calles", rs.getInt("total"));
            } catch (SQLException e) {
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }

    public void mostrarEstadisticas() {
        Map<String, Integer> stats = obtenerEstadisticasMap();
        System.out.println("\n========== ESTADÍSTICAS DE LA BD ==========");
        System.out.println("Total de países: " + stats.getOrDefault("Paises", 0));
        System.out.println("Total de divisiones: " + stats.getOrDefault("Divisiones", 0));
        System.out.println("Total de provincias: " + stats.getOrDefault("Provincias", 0));
        System.out.println("Total de localidades: " + stats.getOrDefault("Localidades", 0));
        System.out.println("Total de habitantes: " + stats.getOrDefault("Habitantes", 0));
        if (stats.containsKey("Capitales")) {
            System.out.println("Total de capitales: " + stats.getOrDefault("Capitales", 0));
        }
        if (stats.containsKey("Calles")) {
            System.out.println("Total de calles: " + stats.getOrDefault("Calles", 0));
        }
        System.out.println("==========================================\n");
    }

    public boolean estaVacia() {
        Map<String, Integer> stats = obtenerEstadisticasMap();
        return stats.getOrDefault("Paises", 0) == 0;
    }

    public void limpiarBaseDatos() {
        String[] tablas = { "numeros", "calles", "localidades", "provincias", "divisiones", "paises" }; // Orden
                                                                                                        // importante
                                                                                                        // por FK
        try (Connection conn = DriverManager.getConnection(URL);
                Statement stmt = conn.createStatement()) {
            for (String tabla : tablas) {
                try {
                    stmt.execute("DELETE FROM " + tabla);
                } catch (SQLException ignored) {
                }
            }
            System.out.println("✓ Base de datos limpiada correctamente.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Carga las calles y edificios de una localidad desde la base de datos a los
     * objetos en memoria.
     * 
     * @param localidad Objeto Localidad al que se añadirán las calles.
     */
    public void cargarCalles(Localidad localidad) {
        String sql = "SELECT cod_via, calle, edificio, tipo_via FROM calle_edificios WHERE localidad_nombre = ?";

        try (Connection conn = DriverManager.getConnection(URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, localidad.getName());
            ResultSet rs = pstmt.executeQuery();

            int count = 0;
            while (rs.next()) {
                String nombreCalle = rs.getString("calle");
                String codVia = rs.getString("cod_via");
                int numEdificio = rs.getInt("edificio");
                String tipoVia = rs.getString("tipo_via");
                if (tipoVia == null)
                    tipoVia = "CL"; // Default to CL if null

                // Buscar si la calle ya existe en la localidad
                Calle calle = localidad.getCalle(nombreCalle);
                if (calle == null) {
                    // Crear nueva calle (ID provisional based on hash)
                    calle = new Calle(nombreCalle.hashCode(), codVia, nombreCalle, tipoVia);
                    localidad.addCalle(calle);
                }

                // Crear y añadir edificio
                // Usamos el codigo postal de la localidad si la dirección no tiene uno
                // específico.
                // Direccion(Calle calle, int numero, String letra, String codigoPostal)
                Direccion dir = new Direccion(calle, numEdificio, "", localidad.getCodigoPostal());
                Edificio edificio = new Edificio(numEdificio, dir); // ID is number here for simplicity? Or hash?
                // Let's use building number as ID if unique in street, but simpler: use hash of
                // street+num
                // Actually Edificio constructor takes ID. using number for now.

                calle.addEdificio(edificio);
                count++;
            }
            if (count > 0) {
                // System.out.println("Cargadas " + count + " direcciones en " +
                // localidad.getName());
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
