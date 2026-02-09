
import civisEconomy.*;
import civisGeo.*;
import civisCitizen.Persona;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class EconomyMain {
    private static EconomyDBManager db;
    private static Scanner scanner;
    private static Map<String, EntidadBancaria> bancos;

    public static void main(String[] args) {
        db = new EconomyDBManager();
        scanner = new Scanner(System.in);

        // Cargar datos al inicio
        System.out.println("Cargando datos del sistema financiero...");
        bancos = db.cargarDatos();
        System.out.println("Datos cargados. Bancos registrados: " + bancos.size());

        menuPrincipal();
    }

    private static void menuPrincipal() {
        boolean salir = false;
        while (!salir) {
            System.out.println("\n=== GESTIÓN ECONÓMICA (CivisEconomy) ===");
            System.out.println("1. Gestión de Bancos");
            System.out.println("2. Gestión de Sucursales");
            System.out.println("3. Gestión de Clientes");
            System.out.println("4. Gestión de Cuentas");
            System.out.println("5. Salir");
            System.out.print("\nSeleccione una opción: ");

            String opcion = scanner.nextLine();

            switch (opcion) {
                case "1":
                    menuBancos();
                    break;
                case "2":
                    menuSucursales();
                    break;
                case "3":
                    menuClientes();
                    break;
                case "4":
                    menuCuentas();
                    break;
                case "5":
                    salir = true;
                    System.out.println("Saliendo del sistema económico...");
                    break;
                default:
                    System.out.println("❌ Opción no válida.");
            }
        }
    }

    // --- BANCOS ---
    private static void menuBancos() {
        System.out.println("\n--- GESTIÓN DE BANCOS ---");
        System.out.println("1. Listar bancos");
        System.out.println("2. Crear nuevo banco");
        System.out.print("Opción: ");
        String op = scanner.nextLine();

        switch (op) {
            case "1":
                listarBancos();
                break;
            case "2":
                crearBanco();
                break;
        }
    }

    private static void listarBancos() {
        if (bancos.isEmpty()) {
            System.out.println("No hay bancos registrados.");
            return;
        }
        for (EntidadBancaria b : bancos.values()) {
            System.out
                    .println("- [" + b.getCodigoBanco() + "] " + b.getNombre() + " (SWIFT: " + b.getSwiftCode() + ")");
        }
    }

    private static void crearBanco() {
        System.out.print("Nombre del banco: ");
        String nombre = scanner.nextLine();
        System.out.print("Código (ID único): ");
        String codigo = scanner.nextLine();
        System.out.print("SWIFT: ");
        String swift = scanner.nextLine();

        if (bancos.containsKey(codigo)) {
            System.out.println("Error: Ya existe un banco con ese código.");
            return;
        }

        EntidadBancaria b = new EntidadBancaria(nombre, codigo, swift, 0.0);
        bancos.put(codigo, b);
        db.guardarBanco(b);
        System.out.println("Banco creado y guardado exitosamente.");
    }

    // --- SUCURSALES ---
    private static void menuSucursales() {
        System.out.println("\n--- GESTIÓN DE SUCURSALES ---");
        System.out.println("1. Listar sucursales de un banco");
        System.out.println("2. Crear nueva sucursal");
        System.out.print("Opción: ");
        String op = scanner.nextLine();

        switch (op) {
            case "1":
                listarSucursales();
                break;
            case "2":
                crearSucursal();
                break;
        }
    }

    private static void listarSucursales() {
        EntidadBancaria b = seleccionarBanco();
        if (b == null)
            return;

        List<Sucursal> sucursales = b.getSucursales();
        if (sucursales.isEmpty()) {
            System.out.println("Este banco no tiene sucursales.");
        } else {
            for (Sucursal s : sucursales) {
                System.out.println("- [" + s.getCodigoSucursal() + "] " + s.getMunicipio().getName() + " ("
                        + s.getMunicipio().getCodigoPostal() + ")");
            }
        }
    }

    private static void crearSucursal() {
        EntidadBancaria b = seleccionarBanco();
        if (b == null)
            return;

        System.out.println("Datos de localización:");
        System.out.print("Nombre Municipio: ");
        String mun = scanner.nextLine();
        System.out.print("Código Postal: ");
        String cp = scanner.nextLine();
        System.out.print("Nombre de Calle: ");
        String calle = scanner.nextLine();

        // Crear objetos geo mínimos
        Localidad loc = new Localidad(mun, cp, 0);
        Calle c = new Calle(calle.hashCode(), "C-GEN", calle, "CL");
        Direccion dir = new Direccion(c, 1, "", cp);
        Edificio ed = new Edificio(1, dir);

        Sucursal s = new Sucursal(b, loc, ed);
        b.agregarSucursal(s);
        db.guardarBanco(b);
        System.out.println("Sucursal creada con código: " + s.getCodigoSucursal());
    }

    // --- CLIENTES ---
    private static void menuClientes() {
        System.out.println("\n--- GESTIÓN DE CLIENTES ---");
        System.out.println("1. Listar clientes de una sucursal");
        System.out.println("2. Crear nuevo cliente");
        System.out.print("Opción: ");
        String op = scanner.nextLine();
        switch (op) {
            case "1":
                listarClientes();
                break;
            case "2":
                crearCliente();
                break;
        }
    }

    private static void listarClientes() {
        Sucursal s = seleccionarSucursal();
        if (s == null)
            return;

        Map<String, Cliente> clientes = s.getClientesAsignados();
        if (clientes.isEmpty()) {
            System.out.println("No hay clientes en esta sucursal.");
        } else {
            for (Cliente c : clientes.values()) {
                System.out.println(
                        "- " + c.getDni() + ": " + c.getUsuario().getName() + " " + c.getUsuario().getFirstSurname());
            }
        }
    }

    private static void crearCliente() {
        Sucursal s = seleccionarSucursal();
        if (s == null)
            return;

        System.out.print("DNI: ");
        String dni = scanner.nextLine();
        System.out.print("Nombre: ");
        String nombre = scanner.nextLine();
        System.out.print("Apellido: ");
        String apellido = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Teléfono: ");
        String tel = scanner.nextLine();

        Persona p = new Persona(nombre, apellido, "", dni, s.getEdificio(), s.getMunicipio(), null, null,
                s.getMunicipio().getCodigoPostal(), tel, email, "1970-01-01", "", "", 'U', 0);
        Cliente c = new Cliente(p, email, tel);

        s.asignarCliente(c);
        db.guardarBanco(s.getBanco());
        System.out.println("Cliente registrado correctamente.");
    }

    // --- CUENTAS ---
    private static void menuCuentas() {
        System.out.println("\n--- GESTIÓN DE CUENTAS ---");
        System.out.println("1. Listar cuentas de una sucursal");
        System.out.println("2. Crear cuenta para un cliente");
        System.out.println("3. Depositar dinero");
        System.out.println("4. Transferir dinero");
        System.out.print("Opción: ");
        String op = scanner.nextLine();

        switch (op) {
            case "1":
                listarCuentas();
                break;
            case "2":
                crearCuenta();
                break;
            case "3":
                depositar();
                break;
            case "4":
                transferir();
                break;
        }
    }

    private static void listarCuentas() {
        Sucursal s = seleccionarSucursal();
        if (s == null)
            return;

        List<CuentaBancaria> cuentas = s.getCuentasAsociadas();
        if (cuentas.isEmpty())
            System.out.println("No hay cuentas registradas.");
        for (CuentaBancaria c : cuentas) {
            System.out.println("- " + c.getNumeroCuenta() + " [" + c.getDivisa() + "] Saldo: " + c.getSaldo());
        }
    }

    private static void crearCuenta() {
        Sucursal s = seleccionarSucursal();
        if (s == null)
            return;

        System.out.print("Ingrese DNI del cliente titular: ");
        String dni = scanner.nextLine();
        Cliente c = s.getClientesAsignados().get(dni);

        if (c == null) {
            System.out.println("Cliente no encontrado en esta sucursal.");
            return;
        }

        System.out.print("Divisa (EUR, USD...): ");
        String divisa = scanner.nextLine();

        s.crearCuentaBancaria(c, divisa);
        db.guardarBanco(s.getBanco());

        // Obtener la última creada
        CuentaBancaria nueva = s.getCuentasAsociadas().get(s.getCuentasAsociadas().size() - 1);
        System.out.println("Cuenta creada con IBAN: " + nueva.getNumeroCuenta());
    }

    private static void depositar() {
        System.out.println("Nota: Para simplificar, buscamos la cuenta por IBAN en todo el sistema.");
        System.out.print("IBAN cuenta destino: ");
        String iban = scanner.nextLine();
        System.out.print("Monto a depositar: ");
        Double monto = Double.parseDouble(scanner.nextLine());

        CuentaBancaria c = buscarCuentaGlobal(iban);
        if (c != null) {
            c.setSaldo(c.getSaldo() + monto);
            // Guardar cambios. Hay que encontrar el banco dueño
            EntidadBancaria b = encontrarBancoDeCuenta(c);
            if (b != null)
                db.guardarBanco(b);
            System.out.println("Nuevo saldo: " + c.getSaldo());
        } else {
            System.out.println("Cuenta no encontrada.");
        }
    }

    private static void transferir() {
        System.out.print("IBAN Origen: ");
        String ib1 = scanner.nextLine();
        System.out.print("IBAN Destino: ");
        String ib2 = scanner.nextLine();
        System.out.print("Monto: ");
        Double monto = Double.parseDouble(scanner.nextLine());

        CuentaBancaria c1 = buscarCuentaGlobal(ib1);
        CuentaBancaria c2 = buscarCuentaGlobal(ib2);

        if (c1 == null || c2 == null) {
            System.out.println("Alguna de las cuentas no existe.");
            return;
        }

        if (c1.realizarTransferencia(c2, monto)) {
            EntidadBancaria b1 = encontrarBancoDeCuenta(c1);
            EntidadBancaria b2 = encontrarBancoDeCuenta(c2);
            if (b1 != null) {
                b1.registrarTransaccion(new Transaccion(c1, c2, monto));
                db.guardarBanco(b1);
            }
            if (b2 != null && b2 != b1) {
                db.guardarBanco(b2);
            }
            System.out.println("Transferencia realizada con éxito.");
        } else {
            System.out.println("Error: Saldo insuficiente o cuenta inactiva.");
        }
    }

    // --- UTILIDADES ---

    private static EntidadBancaria seleccionarBanco() {
        System.out.print("Ingrese Código del Banco: ");
        String cod = scanner.nextLine();
        if (bancos.containsKey(cod))
            return bancos.get(cod);
        System.out.println("Banco no encontrado.");
        return null;
    }

    private static Sucursal seleccionarSucursal() {
        EntidadBancaria b = seleccionarBanco();
        if (b == null)
            return null;

        System.out.print("Ingrese Código de Sucursal: ");
        String codS = scanner.nextLine();

        for (Sucursal s : b.getSucursales()) {
            if (s.getCodigoSucursal().equals(codS))
                return s;
        }
        System.out.println("Sucursal no encontrada en este banco.");
        return null;
    }

    private static CuentaBancaria buscarCuentaGlobal(String iban) {
        for (EntidadBancaria b : bancos.values()) {
            try {
                return b.getCuenta(iban);
            } catch (Exception e) {
            } // Ignorar si no está
        }
        return null;
    }

    private static EntidadBancaria encontrarBancoDeCuenta(CuentaBancaria c) {
        for (EntidadBancaria b : bancos.values()) {
            if (b.getCuentas().containsKey(c.getNumeroCuenta()))
                return b;
        }
        return null;
    }
}
