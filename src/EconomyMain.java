import civisEconomy.*;
import civisGeo.*;
import civisCitizen.Persona;

public class EconomyMain {
    public static void main(String[] args) {
        System.out.println("== Ejemplo terminal: módulo economy ==");

        // 1) Crear entidad bancaria
        EntidadBancaria banco = new EntidadBancaria("BancoDemo", "1234", "BDEMESMM", 0.0);
        System.out.println("Banco creado: " + banco.getNombre() + " (codigo=" + banco.getCodigoBanco() + ")");

        // 2) Crear objetos geográficos necesarios para la sucursal
        Calle calle = new Calle(1, "C001", "Principal", "CL");
        Localidad loc = new Localidad("VillaDemo", "10001", 5000);
        Direccion dir = new Direccion(calle, 10, "A", loc.getCodigoPostal());
        Edificio edificio = new Edificio(1, dir, "Comercial", 1995);

        // 3) Crear sucursal
        Sucursal suc = new Sucursal(banco, loc, edificio);
        banco.agregarSucursal(suc);
        System.out.println("Sucursal creada en municipio: " + suc.getMunicipio().getName() + ", codigo sucursal=" + suc.getCodigoSucursal());

        // 4) Crear persona y cliente
        Persona p = new Persona("Ana", "Gomez", "Perez", "12345678A", edificio, loc, null, null, loc.getCodigoPostal(), "600000000", "ana@example.com", "1980-01-01", "Ciudad", "Española", 'F', 1);
        Cliente cliente = new Cliente(p, p.getEmail(), p.getPhoneNumber());

        // 5) Registrar cliente en el banco
        boolean added = banco.agregarCliente(cliente);
        System.out.println("Cliente agregado al banco: " + added + " (DNI=" + cliente.getUsuario().getDni() + ")");

        // 6) Crear cuenta desde la sucursal y asociarla al banco
        suc.crearCuentaBancaria(cliente, "EUR");
        // Obtener la cuenta recién creada (la última)
        CuentaBancaria cuenta = suc.getCuentasAsociadas().get(0);
        banco.agregarCuenta(cuenta);
        System.out.println("Cuenta creada IBAN=" + cuenta.getNumeroCuenta() + " divisa=" + cuenta.getDivisa());

        // 7) Hacer un abono y mostrar saldos
        cuenta.setSaldo(1500.0);
        System.out.println("Saldo en cuenta origen: " + cuenta.getSaldo());

        // 8) Crear otra cuenta destino
        Persona p2 = new Persona("Luis", "Martinez", "Lopez", "87654321B", edificio, loc, null, null, loc.getCodigoPostal(), "611111111", "luis@example.com", "1975-05-05", "Ciudad", "Española", 'M', 1);
        Cliente cliente2 = new Cliente(p2, p2.getEmail(), p2.getPhoneNumber());
        banco.agregarCliente(cliente2);
        CuentaBancaria cuenta2 = new CuentaBancaria(banco.getCodigoBanco(), suc.getCodigoSucursal(), "EUR");
        cuenta2.setSaldo(200.0);
        banco.agregarCuenta(cuenta2);
        System.out.println("Cuenta destino creada IBAN=" + cuenta2.getNumeroCuenta() + " saldo=" + cuenta2.getSaldo());

        // 9) Realizar transferencia
        double monto = 300.0;
        boolean ok = cuenta.realizarTransferencia(cuenta2, monto);
        System.out.println("Transferencia de " + monto + " realizada: " + ok);
        System.out.println("Saldo origen post: " + cuenta.getSaldo());
        System.out.println("Saldo destino post: " + cuenta2.getSaldo());

        // 10) Registrar transacción en libro mayor del banco
        Transaccion t = new Transaccion(cuenta, cuenta2, monto);
        banco.registrarTransaccion(t);
        System.out.println("Transacción registrada id=" + t.getIdTransaccion());

        System.out.println("== Fin ejemplo economy ==");
    }
}
