package civisEconomy;

import civisCitizen.Persona;
import civisGeo.Calle;
import civisGeo.Direccion;
import civisGeo.Edificio;
import civisGeo.Localidad;

import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class EconomyDBManager {
    private static final String URL = "jdbc:sqlite:civis_economy.db";

    public EconomyDBManager() {
        initDatabase();
    }

    private void initDatabase() {
        try (Connection conn = DriverManager.getConnection(URL);
                Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE TABLE IF NOT EXISTS bancos (" +
                    "codigo TEXT PRIMARY KEY, " +
                    "nombre TEXT NOT NULL, " +
                    "swift TEXT, " +
                    "reservas_liquidas REAL, " +
                    "encaje_legal REAL)");

            stmt.execute("CREATE TABLE IF NOT EXISTS sucursales (" +
                    "codigo TEXT PRIMARY KEY, " +
                    "banco_codigo TEXT NOT NULL, " +
                    "municipio_nombre TEXT, " +
                    "municipio_cp TEXT, " +
                    "calle TEXT, " +
                    "numero INTEGER, " +
                    "num_cajeros INTEGER, " +
                    "FOREIGN KEY(banco_codigo) REFERENCES bancos(codigo))");

            stmt.execute("CREATE TABLE IF NOT EXISTS clientes (" +
                    "dni TEXT PRIMARY KEY, " +
                    "nombre TEXT, " +
                    "apellido TEXT, " +
                    "email TEXT, " +
                    "telefono TEXT, " +
                    "sucursal_codigo TEXT, " +
                    "FOREIGN KEY(sucursal_codigo) REFERENCES sucursales(codigo))");

            stmt.execute("CREATE TABLE IF NOT EXISTS cuentas (" +
                    "iban TEXT PRIMARY KEY, " +
                    "saldo REAL, " +
                    "divisa TEXT, " +
                    "fecha_apertura TEXT, " +
                    "activa INTEGER, " +
                    "limite_diario REAL, " +
                    "sucursal_codigo TEXT, " +
                    "FOREIGN KEY(sucursal_codigo) REFERENCES sucursales(codigo))");

            stmt.execute("CREATE TABLE IF NOT EXISTS cuentas_clientes (" +
                    "cuenta_iban TEXT, " +
                    "cliente_dni TEXT, " +
                    "PRIMARY KEY(cuenta_iban, cliente_dni), " +
                    "FOREIGN KEY(cuenta_iban) REFERENCES cuentas(iban), " +
                    "FOREIGN KEY(cliente_dni) REFERENCES clientes(dni))");

            stmt.execute("CREATE TABLE IF NOT EXISTS transacciones (" +
                    "id TEXT PRIMARY KEY, " +
                    "origen_iban TEXT, " +
                    "destino_iban TEXT, " +
                    "monto REAL, " +
                    "fecha TEXT, " +
                    "FOREIGN KEY(origen_iban) REFERENCES cuentas(iban), " +
                    "FOREIGN KEY(destino_iban) REFERENCES cuentas(iban))");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void guardarBanco(EntidadBancaria banco) {
        String sql = "INSERT OR REPLACE INTO bancos (codigo, nombre, swift, reservas_liquidas, encaje_legal) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, banco.getCodigoBanco());
            pstmt.setString(2, banco.getNombre());
            pstmt.setString(3, banco.getSwiftCode());
            pstmt.setDouble(4, banco.getReservasLiquidas());
            pstmt.setDouble(5, banco.getEncajeLegal());
            pstmt.executeUpdate();

            for (Sucursal s : banco.getSucursales()) {
                guardarSucursal(s, conn);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void guardarSucursal(Sucursal s, Connection conn) throws SQLException {
        String sql = "INSERT OR REPLACE INTO sucursales (codigo, banco_codigo, municipio_nombre, municipio_cp, calle, numero, num_cajeros) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, s.getCodigoSucursal());
            pstmt.setString(2, s.getBanco().getCodigoBanco());
            pstmt.setString(3, s.getMunicipio().getName());
            pstmt.setString(4, s.getMunicipio().getCodigoPostal());

            String calle = "Principal";
            int numero = 1;
            if (s.getEdificio() != null && s.getEdificio().getDireccion() != null) {
                if (s.getEdificio().getDireccion().getCalle() != null)
                    calle = s.getEdificio().getDireccion().getCalle().getNombre();
                numero = s.getEdificio().getDireccion().getNumero();
            }
            pstmt.setString(5, calle);
            pstmt.setInt(6, numero);
            pstmt.setInt(7, s.getNumCajeros());
            pstmt.executeUpdate();

            for (Cliente c : s.getClientesAsignados().values()) {
                guardarCliente(c, s.getCodigoSucursal(), conn);
            }

            for (CuentaBancaria c : s.getCuentasAsociadas()) {
                guardarCuenta(c, s.getCodigoSucursal(), conn);
            }
        }
    }

    private void guardarCliente(Cliente c, String sucursalCodigo, Connection conn) throws SQLException {
        String sql = "INSERT OR REPLACE INTO clientes (dni, nombre, apellido, email, telefono, sucursal_codigo) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, c.getDni());
            pstmt.setString(2, c.getUsuario().getName());

            String apellido = c.getUsuario().getFirstSurname();
            if (c.getUsuario().getSecondSurname() != null && !c.getUsuario().getSecondSurname().isEmpty()) {
                apellido += " " + c.getUsuario().getSecondSurname();
            }
            pstmt.setString(3, apellido);

            pstmt.setString(4, c.getEmail());
            pstmt.setString(5, c.getTelefono());
            pstmt.setString(6, sucursalCodigo);
            pstmt.executeUpdate();
        }
    }

    private void guardarCuenta(CuentaBancaria c, String sucursalCodigo, Connection conn) throws SQLException {
        String sql = "INSERT OR REPLACE INTO cuentas (iban, saldo, divisa, fecha_apertura, activa, limite_diario, sucursal_codigo) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, c.getNumeroCuenta());
            pstmt.setDouble(2, c.getSaldo());
            pstmt.setString(3, c.getDivisa());
            pstmt.setString(4, c.getFechaApertura().toString());
            pstmt.setInt(5, 1);
            pstmt.setDouble(6, c.getLimiteDiarioCajero());
            pstmt.setString(7, sucursalCodigo);
            pstmt.executeUpdate();

            guardarTitulares(c, conn);
        }
    }

    private void guardarTitulares(CuentaBancaria c, Connection conn) throws SQLException {
        String sqlDelete = "DELETE FROM cuentas_clientes WHERE cuenta_iban = ?";
        try (PreparedStatement pdel = conn.prepareStatement(sqlDelete)) {
            pdel.setString(1, c.getNumeroCuenta());
            pdel.executeUpdate();
        }

        String sql = "INSERT INTO cuentas_clientes (cuenta_iban, cliente_dni) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (String dni : c.getTitulares().keySet()) {
                pstmt.setString(1, c.getNumeroCuenta());
                pstmt.setString(2, dni);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    public Map<String, EntidadBancaria> cargarDatos() {
        Map<String, EntidadBancaria> bancos = new HashMap<>();
        String sqlBancos = "SELECT * FROM bancos";

        try (Connection conn = DriverManager.getConnection(URL);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sqlBancos)) {

            while (rs.next()) {
                EntidadBancaria eb = new EntidadBancaria(
                        rs.getString("nombre"),
                        rs.getString("codigo"),
                        rs.getString("swift"),
                        rs.getDouble("encaje_legal"));
                cargarSucursales(eb, conn);
                bancos.put(eb.getCodigoBanco(), eb);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bancos;
    }

    private void cargarSucursales(EntidadBancaria banco, Connection conn) throws SQLException {
        String sql = "SELECT * FROM sucursales WHERE banco_codigo = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, banco.getCodigoBanco());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String locNombre = rs.getString("municipio_nombre");
                String cp = rs.getString("municipio_cp");
                String calleC = rs.getString("calle");
                int num = rs.getInt("numero");
                String codigoSucursal = rs.getString("codigo");

                Localidad loc = new Localidad(locNombre, cp, 0);
                Calle calle = new Calle(calleC.hashCode(), "C-GEN", calleC, "CL");
                Direccion dir = new Direccion(calle, num, "", cp);
                Edificio ed = new Edificio(num, dir);

                // Usamos el constructor estándar que autogenera el código
                Sucursal sucursal = new Sucursal(banco, loc, ed);

                // Usamos el codigoSucursal (leido de BD) para buscar hijos,
                // ya que el generado nuevo podría diferir si el orden cambió.
                cargarClientes(sucursal, codigoSucursal, conn);
                cargarCuentas(sucursal, codigoSucursal, conn);

                banco.agregarSucursal(sucursal);
            }
        }
    }

    private void cargarClientes(Sucursal sucursal, String codigoDB, Connection conn) throws SQLException {
        String sql = "SELECT * FROM clientes WHERE sucursal_codigo = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, codigoDB);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String dni = rs.getString("dni");
                String nom = rs.getString("nombre");
                String ape = rs.getString("apellido"); // Combinado o first surname
                String email = rs.getString("email");
                String tel = rs.getString("telefono");

                Persona p = new Persona(nom, ape, "", dni, sucursal.getEdificio(), sucursal.getMunicipio(), null, null,
                        sucursal.getMunicipio().getCodigoPostal(), tel, email, "1970-01-01", "", "", 'U', 0);
                Cliente c = new Cliente(p, email, tel);
                sucursal.asignarCliente(c);
            }
        }
    }

    private void cargarCuentas(Sucursal sucursal, String codigoDB, Connection conn) throws SQLException {
        String sql = "SELECT * FROM cuentas WHERE sucursal_codigo = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, codigoDB);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String iban = rs.getString("iban");
                double saldo = rs.getDouble("saldo");
                String divisa = rs.getString("divisa");
                double limite = rs.getDouble("limite_diario");
                String fechaStr = rs.getString("fecha_apertura");
                LocalDate fecha = LocalDate.now();
                try {
                    fecha = LocalDate.parse(fechaStr);
                } catch (Exception e) {
                }

                CuentaBancaria cuenta = new CuentaBancaria(iban, saldo, divisa, fecha, limite);

                cargarTitulares(cuenta, sucursal, conn);

                sucursal.getCuentasAsociadas().add(cuenta);
                sucursal.getBanco().agregarCuenta(cuenta);

                for (String dni : cuenta.getTitulares().keySet()) {
                    Cliente cli = sucursal.getClientesAsignados().get(dni);
                    if (cli != null) {
                        cli.agregarCuenta(cuenta);
                    }
                }
            }
        }
    }

    private void cargarTitulares(CuentaBancaria cuenta, Sucursal sucursal, Connection conn) throws SQLException {
        String sql = "SELECT cliente_dni FROM cuentas_clientes WHERE cuenta_iban = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, cuenta.getNumeroCuenta());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String dni = rs.getString("cliente_dni");
                Cliente c = sucursal.getClientesAsignados().get(dni);
                if (c != null) {
                    cuenta.agregarTitular(c);
                }
            }
        }
    }
}
