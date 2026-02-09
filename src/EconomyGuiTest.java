import civisEconomy.*;
import civisGeo.*;
import civisCitizen.Persona;

import javax.swing.*;
import java.awt.*;

public class EconomyGuiTest extends JFrame {
    private JTextArea log;
    private EntidadBancaria banco;
    private Sucursal suc;
    private CuentaBancaria cuentaOrigen;
    private CuentaBancaria cuentaDestino;

    public EconomyGuiTest() {
        setTitle("Economy GUI Test");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel p = new JPanel(new BorderLayout());
        log = new JTextArea();
        log.setEditable(false);
        p.add(new JScrollPane(log), BorderLayout.CENTER);

        JPanel controls = new JPanel(new FlowLayout());
        JButton btnInit = new JButton("Init Banco/Sucursal");
        JButton btnCreate = new JButton("Crear Cliente y Cuenta");
        JButton btnTransfer = new JButton("Transferir 300 EUR");

        btnInit.addActionListener(e -> initBanco());
        btnCreate.addActionListener(e -> crearClienteYCuenta());
        btnTransfer.addActionListener(e -> transferir());

        controls.add(btnInit);
        controls.add(btnCreate);
        controls.add(btnTransfer);

        p.add(controls, BorderLayout.NORTH);
        setContentPane(p);
    }

    private void initBanco() {
        banco = new EntidadBancaria("BancoGUI", "4321", "BGUIMMSM", 0.0);
        Calle calle = new Calle(1, "C010", "Demo", "CL");
        Localidad loc = new Localidad("GuiTown", "20002", 1200);
        Direccion dir = new Direccion(calle, 5, "", loc.getCodigoPostal());
        Edificio edificio = new Edificio(2, dir, "Oficina", 2000);
        suc = new Sucursal(banco, loc, edificio);
        banco.agregarSucursal(suc);
        log.append("Banco y sucursal inicializados. Sucursal: " + suc.getCodigoSucursal() + "\n");
    }

    private void crearClienteYCuenta() {
        if (banco == null) { JOptionPane.showMessageDialog(this, "Inicialice el banco primero."); return; }
        Persona p = new Persona("Marta", "Diaz", "N/A", "55555555X", suc.getEdificio(), suc.getMunicipio(), null, null, suc.getMunicipio().getCodigoPostal(), "622222222", "marta@example.com", "1990-02-02", "Lugar", "Española", 'F', 1);
        Cliente c = new Cliente(p, p.getEmail(), p.getPhoneNumber());
        banco.agregarCliente(c);
        suc.crearCuentaBancaria(c, "EUR");
        cuentaOrigen = suc.getCuentasAsociadas().get(0);
        cuentaOrigen.setSaldo(1000.0);
        log.append("Cliente y cuenta creados. IBAN: " + cuentaOrigen.getNumeroCuenta() + " Saldo: " + cuentaOrigen.getSaldo() + "\n");

        // crear cuenta destino
        Persona p2 = new Persona("Paco", "Lopez", "N/A", "66666666Y", suc.getEdificio(), suc.getMunicipio(), null, null, suc.getMunicipio().getCodigoPostal(), "633333333", "paco@example.com", "1988-03-03", "Lugar", "Española", 'M', 1);
        Cliente c2 = new Cliente(p2, p2.getEmail(), p2.getPhoneNumber());
        banco.agregarCliente(c2);
        cuentaDestino = new CuentaBancaria(banco.getCodigoBanco(), suc.getCodigoSucursal(), "EUR");
        cuentaDestino.setSaldo(50.0);
        banco.agregarCuenta(cuentaDestino);
        log.append("Cuenta destino creada. IBAN: " + cuentaDestino.getNumeroCuenta() + " Saldo: " + cuentaDestino.getSaldo() + "\n");
    }

    private void transferir() {
        if (cuentaOrigen == null || cuentaDestino == null) { JOptionPane.showMessageDialog(this, "Cree las cuentas primero."); return; }
        double m = 300.0;
        boolean ok = cuentaOrigen.realizarTransferencia(cuentaDestino, m);
        log.append("Transferencia " + m + " -> " + (ok ? "OK" : "FALLÓ") + "\n");
        log.append("Origen saldo: " + cuentaOrigen.getSaldo() + " | Destino saldo: " + cuentaDestino.getSaldo() + "\n");
        if (ok) {
            Transaccion t = new Transaccion(cuentaOrigen, cuentaDestino, m);
            banco.registrarTransaccion(t);
            log.append("Transacción registrada: " + t.getIdTransaccion() + "\n");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new EconomyGuiTest().setVisible(true));
    }
}
