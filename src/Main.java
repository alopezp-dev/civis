
import java.util.List;
import java.util.Scanner;

public class Main {
    private static DBManager db;
    private static Scanner scanner;

    public static void main(String[] args) {
        db = new DBManager();
        scanner = new Scanner(System.in);

        mostrarBienvenida();
        menuPrincipal();
    }

    private static void mostrarBienvenida() {
        System.out.println("\n╔══════════════════════════════════════╗");
        System.out.println("║  SISTEMA DE GESTIÓN TERRITORIAL      ║");
        System.out.println("║           (CivisGeo)                 ║");
        System.out.println("╚══════════════════════════════════════╝\n");
    }

    private static void menuPrincipal() {
        boolean salir = false;
        while (!salir) {
            System.out.println("\n=== MENÚ PRINCIPAL ===");
            System.out.println("1. Importar datos desde CSV");
            System.out.println("2. Realizar consultas");
            System.out.println("3. Buscar localidad por nombre");
            System.out.println("4. Mostrar estadísticas");
            System.out.println("5. Limpiar base de datos");
            System.out.println("6. Salir");
            System.out.print("\nSeleccione una opción: ");

            String opcion = scanner.nextLine();

            switch (opcion) {
                case "1":
                    menuImportar();
                    break;
                case "2":
                    menuConsultas();
                    break;
                case "3":
                    buscarLocalidad();
                    break;
                case "4":
                    db.mostrarEstadisticas();
                    break;
                case "5":
                    limpiarBaseDatos();
                    break;
                case "6":
                    salir = true;
                    despedida();
                    break;
                default:
                    System.out.println("❌ Opción no válida. Intente de nuevo.");
            }
        }
    }

    private static void menuImportar() {
        System.out.println("\n=== IMPORTAR DATOS ===");
        System.out.println("1. Importar países");
        System.out.println("2. Importar divisiones");
        System.out.println("3. Importar provincias");
        System.out.println("4. Importar localidades");
        System.out.println("5. Importar capitales");
        System.out.println("6. Importar datos censo (Calles/Edificios)");
        System.out.println("7. Importar todo (en orden)");
        System.out.println("8. Volver al menú principal");
        System.out.print("\nSeleccione una opción: ");

        String opcion = scanner.nextLine();

        switch (opcion) {
            case "1":
                importarArchivo("csv/paises.csv", "países");
                break;
            case "2":
                importarArchivo("csv/divisiones.csv", "divisiones");
                break;
            case "3":
                importarArchivo("csv/provincias.csv", "provincias");
                break;
            case "4":
                importarArchivo("csv/localidades.csv", "localidades");
                break;
            case "5":
                importarArchivo("csv/capitales.csv", "capitales");
                break;
            case "6":
                // Census data import
                importarDatosCenso();
                break;
            case "7":
                importarTodo();
                break;
            case "8":
                break;
            default:
                System.out.println("❌ Opción no válida.");
        }
    }

    private static void importarArchivo(String ruta, String tipo) {
        System.out.print("\n¿Desea importar " + tipo + " desde " + ruta + "? (s/n): ");
        String confirmacion = scanner.nextLine();

        if (confirmacion.equalsIgnoreCase("s")) {
            int registros = 0;
            if (tipo.equals("países")) {
                registros = db.importarPaisesCSV(ruta);
            } else if (tipo.equals("divisiones")) {
                registros = db.importarDivisionesCSV(ruta);
            } else if (tipo.equals("provincias")) {
                registros = db.importarProvinciasCSV(ruta);
            } else if (tipo.equals("localidades")) {
                registros = db.importarLocalidadesCSV(ruta);
            } else if (tipo.equals("capitales")) {
                registros = db.importarCapitalesCSV(ruta);
            }
            System.out.println("✓ Importación completada: " + registros + " registros importados.");
        } else {
            System.out.println("Importación cancelada.");
        }
    }

    private static void importarTodo() {
        System.out.println("\nImportando datos completos...");
        System.out.println("Esto importará países, divisiones, provincias, localidades y capitales.\n");
        System.out.print("¿Desea continuar? (s/n): ");
        String confirmacion = scanner.nextLine();

        if (confirmacion.equalsIgnoreCase("s")) {
            System.out.println("\n1. Importando países...");
            db.importarPaisesCSV("csv/paises.csv");

            System.out.println("2. Importando divisiones...");
            db.importarDivisionesCSV("csv/divisiones.csv");

            System.out.println("3. Importando provincias...");
            db.importarProvinciasCSV("csv/provincias.csv");

            System.out.println("4. Importando localidades...");
            db.importarLocalidadesCSV("csv/localidades.csv");

            System.out.println("5. Importando capitales...");
            db.importarCapitalesCSV("csv/capitales.csv");

            System.out.println("6. Importando datos del censo...");
            db.importarDatosCenso("census.db");

            System.out.println("\n✓ Importación completa finalizada correctamente.");
            db.mostrarEstadisticas();
        } else {
            System.out.println("Importación cancelada.");
        }
    }

    private static void importarDatosCenso() {
        System.out.print("\n¿Desea importar datos del censo (Calles y Edificios) desde census.db? (s/n): ");
        String confirmacion = scanner.nextLine();

        if (confirmacion.equalsIgnoreCase("s")) {
            db.importarDatosCenso("census.db");
            System.out.println("✓ Importación de datos censales finalizada.");
        } else {
            System.out.println("Importación cancelada.");
        }
    }

    private static void menuConsultas() {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n=== CONSULTAS ===");
            System.out.println("1. Listar todos los países");
            System.out.println("2. Listar divisiones de un país");
            System.out.println("3. Listar provincias de una división");
            System.out.println("4. Listar localidades de una provincia");
            System.out.println("5. Listar principales localidades (top 20)");
            System.out.println("6. Volver al menú principal");
            System.out.print("\nSeleccione una opción: ");

            String opcion = scanner.nextLine();

            switch (opcion) {
                case "1":
                    consultarPaises();
                    break;
                case "2":
                    consultarDivisiones();
                    break;
                case "3":
                    consultarProvincias();
                    break;
                case "4":
                    consultarLocalidades();
                    break;
                case "5":
                    consultarPrincipalesLocalidades();
                    break;
                case "6":
                    volver = true;
                    break;
                default:
                    System.out.println("❌ Opción no válida.");
            }
        }
    }

    private static void consultarPaises() {
        List<String> paises = db.obtenerPaises();
        if (paises.isEmpty()) {
            System.out.println("\n⚠️  No hay países en la base de datos.");
        } else {
            System.out.println("\n=== PAÍSES ===");
            for (int i = 0; i < paises.size(); i++) {
                System.out.println((i + 1) + ". " + paises.get(i));
            }
        }
    }

    private static void consultarDivisiones() {
        List<String> paises = db.obtenerPaises();
        if (paises.isEmpty()) {
            System.out.println("\n⚠️  No hay países en la base de datos.");
            return;
        }

        System.out.println("\n=== PAÍSES ===");
        for (int i = 0; i < paises.size(); i++) {
            System.out.println((i + 1) + ". " + paises.get(i));
        }

        System.out.print("\nSeleccione un país (número): ");
        try {
            int indice = Integer.parseInt(scanner.nextLine()) - 1;
            if (indice >= 0 && indice < paises.size()) {
                String paisSeleccionado = paises.get(indice);
                List<String[]> divisiones = db.obtenerDivisiones(paisSeleccionado);

                if (divisiones.isEmpty()) {
                    System.out.println("\n⚠️  No hay divisiones para " + paisSeleccionado);
                } else {
                    System.out.println("\n=== DIVISIONES DE " + paisSeleccionado.toUpperCase() + " ===");
                    for (int i = 0; i < divisiones.size(); i++) {
                        System.out.println((i + 1) + ". " + divisiones.get(i)[0] + " -> " + divisiones.get(i)[1]);
                    }
                }
            } else {
                System.out.println("❌ Selección inválida.");
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ Entrada inválida.");
        }
    }

    private static void consultarProvincias() {
        System.out.print("\nIngrese el nombre de la división: ");
        String division = scanner.nextLine();

        List<String[]> provincias = db.obtenerProvincias(division);
        if (provincias.isEmpty()) {
            System.out.println("\n⚠️  No hay provincias para la división '" + division + "'");
        } else {
            System.out.println("\n=== PROVINCIAS DE " + division.toUpperCase() + " ===");
            for (int i = 0; i < provincias.size(); i++) {
                System.out.println((i + 1) + ". " + provincias.get(i)[0]);
            }
        }
    }

    private static void consultarLocalidades() {
        System.out.print("\nIngrese el nombre de la provincia: ");
        String provincia = scanner.nextLine();

        // data: [Nombre, Hab, Prov, CP, Capital]
        List<String[]> localidades = db.obtenerLocalidadesDeProvincia(provincia);
        if (localidades.isEmpty()) {
            System.out.println("\n⚠️  No hay localidades para la provincia '" + provincia + "'");
        } else {
            System.out.println("\n=== LOCALIDADES DE " + provincia.toUpperCase() + " ===");
            for (int i = 0; i < localidades.size(); i++) {
                String[] loc = localidades.get(i);
                String capitalTag = loc[4].isEmpty() ? "" : " [" + loc[4] + "]";
                System.out.printf("%d. %s (%s hab) [CP: %s]%s\n", (i + 1), loc[0], loc[1], loc[3], capitalTag);
            }
        }
    }

    private static void consultarPrincipalesLocalidades() {
        List<String[]> localidades = db.obtenerLocalidades();
        if (localidades.isEmpty()) {
            System.out.println("\n⚠️  No hay localidades en la base de datos.");
        } else {
            System.out.println("\n=== TOP 20 LOCALIDADES (POR HABITANTES) ===");
            for (int i = 0; i < localidades.size(); i++) {
                String[] loc = localidades.get(i);
                String capitalTag = loc[4].isEmpty() ? "" : " [" + loc[4] + "]";
                System.out.printf("%d. %s (%s hab) - %s%s\n", (i + 1), loc[0], loc[1], loc[2], capitalTag);
            }
        }
    }

    private static void buscarLocalidad() {
        System.out.print("\nIngrese nombre de localidad a buscar: ");
        String busqueda = scanner.nextLine();

        List<String[]> resultados = db.buscarLocalidades(busqueda);
        if (resultados.isEmpty()) {
            System.out.println("\n⚠️  No se encontraron coincidencias.");
        } else {
            System.out.println("\n=== RESULTADOS DE BÚSQUEDA ===");
            for (String[] loc : resultados) {
                // loc: [nombre, habitantes, provincia, cp, capital_tipo]
                String capitalTag = loc[4].isEmpty() ? "" : " [" + loc[4] + "]";
                System.out.printf("- %s (%s hab) [Prov: %s, CP: %s]%s\n", loc[0], loc[1], loc[2], loc[3], capitalTag);
            }
        }
    }

    private static void limpiarBaseDatos() {
        System.out.print("\n⚠️  ¿Está seguro de que desea limpiar la base de datos? (s/n): ");
        String confirmacion = scanner.nextLine();

        if (confirmacion.equalsIgnoreCase("s")) {
            System.out.print("Escriba 'CONFIRMAR' para borrar toda la información: ");
            String confirmacion2 = scanner.nextLine();

            if (confirmacion2.equals("CONFIRMAR")) {
                db.limpiarBaseDatos();
                System.out.println("✓ Base de datos limpiada exitosamente.");
            } else {
                System.out.println("Operación cancelada.");
            }
        } else {
            System.out.println("Operación cancelada.");
        }
    }

    private static void despedida() {
        System.out.println("\n╔══════════════════════════════════════╗");
        System.out.println("║   ¡Gracias por usar CivisGeo!        ║");
        System.out.println("║        ¡Hasta pronto!                ║");
        System.out.println("╚══════════════════════════════════════╝\n");
        scanner.close();
    }
}
