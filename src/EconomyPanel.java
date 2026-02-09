
import civisGeo.*;
import civisCitizen.Persona;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import civisEconomy.*;

public class EconomyPanel extends JPanel {
    // Models and maps
    private DefaultListModel<String> banksModel = new DefaultListModel<>();
    private Map<String, EntidadBancaria> banks = new HashMap<>();

    private DefaultListModel<String> branchesModel = new DefaultListModel<>();
    private Map<String, Sucursal> branches = new HashMap<>();

    private DefaultListModel<String> clientsModel = new DefaultListModel<>();
    private DefaultListModel<String> accountsModel = new DefaultListModel<>();

    // UI
    private JList<String> lstBanks;
    private JList<String> lstBranches;
    private JList<String> lstClients;
    private JList<String> lstAccounts;
    private JTextArea log;

    public EconomyPanel() {
        setLayout(new BorderLayout(8,8));

        // Left: Banks
        JPanel left = new JPanel(new BorderLayout(4,4));
        left.setBorder(BorderFactory.createTitledBorder("Bancos"));
        lstBanks = new JList<>(banksModel);
        left.add(new JScrollPane(lstBanks), BorderLayout.CENTER);
        JPanel leftBtns = new JPanel(new GridLayout(0,1,4,4));
        JButton btnAddBank = new JButton("Crear Banco");
        JButton btnRemoveBank = new JButton("Eliminar Banco");
        leftBtns.add(btnAddBank);
        leftBtns.add(btnRemoveBank);
        left.add(leftBtns, BorderLayout.SOUTH);

        // Middle: Branches
        JPanel mid = new JPanel(new BorderLayout(4,4));
        mid.setBorder(BorderFactory.createTitledBorder("Sucursales"));
        lstBranches = new JList<>(branchesModel);
        mid.add(new JScrollPane(lstBranches), BorderLayout.CENTER);
        JPanel midBtns = new JPanel(new GridLayout(0,1,4,4));
        JButton btnAddBranch = new JButton("Crear Sucursal");
        JButton btnRemoveBranch = new JButton("Eliminar Sucursal");
        midBtns.add(btnAddBranch);
        midBtns.add(btnRemoveBranch);
        mid.add(midBtns, BorderLayout.SOUTH);

        // Right: Branch details (clients/accounts)
        JPanel right = new JPanel(new BorderLayout(4,4));
        right.setBorder(BorderFactory.createTitledBorder("Operaciones en Sucursal"));

        JPanel lists = new JPanel(new GridLayout(2,1,4,4));
        lstClients = new JList<>(clientsModel);
        lstAccounts = new JList<>(accountsModel);
        lists.add(new JScrollPane(lstClients));
        lists.add(new JScrollPane(lstAccounts));
        right.add(lists, BorderLayout.CENTER);

        JPanel ops = new JPanel(new GridLayout(0,1,4,4));
        JButton btnAddClient = new JButton("Agregar Cliente");
        JButton btnAddAccount = new JButton("Crear Cuenta para Cliente");
        JButton btnDeposit = new JButton("Depositar (100)");
        JButton btnTransfer = new JButton("Transferir (300)");
        ops.add(btnAddClient);
        ops.add(btnAddAccount);
        ops.add(btnDeposit);
        ops.add(btnTransfer);
        right.add(ops, BorderLayout.SOUTH);

        // Bottom: log
        log = new JTextArea(6,40);
        log.setEditable(false);

        JSplitPane topSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, mid);
        topSplit.setResizeWeight(0.3);
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, topSplit, right);
        mainSplit.setResizeWeight(0.7);

        add(mainSplit, BorderLayout.CENTER);
        add(new JScrollPane(log), BorderLayout.SOUTH);

        // Listeners
        btnAddBank.addActionListener(e -> createBank());
        btnRemoveBank.addActionListener(e -> removeSelectedBank());
        btnAddBranch.addActionListener(e -> createBranch());
        btnRemoveBranch.addActionListener(e -> removeSelectedBranch());

        lstBanks.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) onBankSelected();
            }
        });

        lstBranches.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) onBranchSelected();
            }
        });

        btnAddClient.addActionListener(e -> addClientToSelectedBranch());
        btnAddAccount.addActionListener(e -> createAccountForSelectedClient());
        btnDeposit.addActionListener(e -> depositToSelectedAccount(100.0));
        btnTransfer.addActionListener(e -> transferBetweenAccounts(300.0));
    }

    // UI actions
    private void createBank() {
        JTextField name = new JTextField();
        JTextField code = new JTextField();
        JTextField swift = new JTextField();
        Object[] fields = {"Nombre:", name, "Código banco (ej: 1234):", code, "SWIFT:", swift};
        int ok = JOptionPane.showConfirmDialog(this, fields, "Crear Banco", JOptionPane.OK_CANCEL_OPTION);
        if (ok == JOptionPane.OK_OPTION) {
            String n = name.getText().trim();
            String c = code.getText().trim();
            String s = swift.getText().trim();
            if (n.isEmpty() || c.isEmpty()) { JOptionPane.showMessageDialog(this, "Nombre y código obligatorios."); return; }
            if (banks.containsKey(c)) { JOptionPane.showMessageDialog(this, "Ya existe un banco con ese código."); return; }
            EntidadBancaria b = new EntidadBancaria(n, c, s, 0.0);
            banks.put(c, b);
            banksModel.addElement(c + " - " + n);
            log.append("Banco creado: " + n + " (" + c + ")\n");
        }
    }

    private void removeSelectedBank() {
        String sel = lstBanks.getSelectedValue();
        if (sel == null) return;
        String key = sel.split(" - ")[0];
        EntidadBancaria b = banks.remove(key);
        banksModel.removeElement(sel);
        branchesModel.clear(); branches.clear(); clientsModel.clear(); accountsModel.clear();
        log.append("Banco eliminado: " + sel + "\n");
    }

    private void createBranch() {
        String bankSel = lstBanks.getSelectedValue();
        if (bankSel == null) { JOptionPane.showMessageDialog(this, "Seleccione un banco primero."); return; }
        String bankKey = bankSel.split(" - ")[0];
        EntidadBancaria b = banks.get(bankKey);

        JTextField codigo = new JTextField();
        JTextField localidad = new JTextField();
        JTextField cp = new JTextField();
        JTextField calle = new JTextField();
        Object[] fields = {"Código sucursal (4 dígitos opcional):", codigo, "Localidad:", localidad, "Código postal:", cp, "Nombre de calle:", calle};
        int ok = JOptionPane.showConfirmDialog(this, fields, "Crear Sucursal", JOptionPane.OK_CANCEL_OPTION);
        if (ok == JOptionPane.OK_OPTION) {
            String cod = codigo.getText().trim();
            String locName = localidad.getText().trim();
            String cpv = cp.getText().trim();
            String calleName = calle.getText().trim();
            if (locName.isEmpty() || cpv.isEmpty()) { JOptionPane.showMessageDialog(this, "Localidad y CP obligatorios."); return; }
            // Build minimal geo objects
            Calle c = new Calle(1, "C-1", calleName.isEmpty()?"Principal":calleName, "CL");
            Localidad loc = new Localidad(locName, cpv, 0);
            Direccion dir = new Direccion(c, 1, "", cpv);
            Edificio ed = new Edificio(1, dir);
            Sucursal s;
            if (cod.isEmpty()) s = new Sucursal(b, loc, ed);
            else s = new Sucursal(b, loc, ed);
            b.agregarSucursal(s);
            String display = b.getCodigoBanco() + ":" + s.getCodigoSucursal() + " - " + locName;
            branches.put(display, s);
            branchesModel.addElement(display);
            log.append("Sucursal creada: " + display + "\n");
        }
    }

    private void removeSelectedBranch() {
        String sel = lstBranches.getSelectedValue();
        if (sel == null) return;
        Sucursal s = branches.remove(sel);
        branchesModel.removeElement(sel);
        clientsModel.clear(); accountsModel.clear();
        // Also remove from bank list in EntidadBancaria
        // find bank by prefix
        String bankCode = sel.split(":")[0];
        EntidadBancaria b = banks.get(bankCode);
        if (b != null) b.getSucursales().remove(s);
        log.append("Sucursal eliminada: " + sel + "\n");
    }

    private void onBankSelected() {
        branchesModel.clear(); branches.clear(); clientsModel.clear(); accountsModel.clear();
        String sel = lstBanks.getSelectedValue();
        if (sel == null) return;
        String key = sel.split(" - ")[0];
        EntidadBancaria b = banks.get(key);
        for (Sucursal s : b.getSucursales()) {
            String display = b.getCodigoBanco() + ":" + s.getCodigoSucursal() + " - " + s.getMunicipio().getName();
            branches.put(display, s);
            branchesModel.addElement(display);
        }
        log.append("Banco seleccionado: " + b.getNombre() + "\n");
    }

    private void onBranchSelected() {
        clientsModel.clear(); accountsModel.clear();
        String sel = lstBranches.getSelectedValue();
        if (sel == null) return;
        Sucursal s = branches.get(sel);
        // mostrar clientes
        for (Map.Entry<String, Cliente> e : s.getClientesAsignados().entrySet()) {
            clientsModel.addElement(e.getKey() + " - " + e.getValue().getDni());
        }
        // mostrar cuentas
        for (CuentaBancaria c : s.getCuentasAsociadas()) {
            accountsModel.addElement(c.getNumeroCuenta() + " (" + c.getDivisa() + ") -> " + c.getSaldo());
        }
        log.append("Sucursal seleccionada: " + sel + "\n");
    }

    private void addClientToSelectedBranch() {
        String sel = lstBranches.getSelectedValue();
        if (sel == null) { JOptionPane.showMessageDialog(this, "Seleccione una sucursal."); return; }
        Sucursal s = branches.get(sel);
        JTextField nombre = new JTextField();
        JTextField apellido = new JTextField();
        JTextField dni = new JTextField();
        JTextField phone = new JTextField();
        JTextField email = new JTextField();
        Object[] fields = {"Nombre:", nombre, "Apellido:", apellido, "DNI:", dni, "Tel:", phone, "Email:", email};
        int ok = JOptionPane.showConfirmDialog(this, fields, "Agregar Cliente", JOptionPane.OK_CANCEL_OPTION);
        if (ok == JOptionPane.OK_OPTION) {
            Persona p = new Persona(nombre.getText().trim(), apellido.getText().trim(), "", dni.getText().trim(), s.getEdificio(), s.getMunicipio(), null, null, s.getMunicipio().getCodigoPostal(), phone.getText().trim(), email.getText().trim(), "1970-01-01", "", "", 'U', 0);
            Cliente c = new Cliente(p, p.getEmail(), p.getPhoneNumber());
            s.asignarCliente(c);
            s.getClientesAsignados().put(c.getDni(), c);
            clientsModel.addElement(c.getDni() + " - " + c.getDni());
            log.append("Cliente agregado: " + c.getDni() + "\n");
        }
    }

    private void createAccountForSelectedClient() {
        String selBranch = lstBranches.getSelectedValue();
        String selClient = lstClients.getSelectedValue();
        if (selBranch == null || selClient == null) { JOptionPane.showMessageDialog(this, "Seleccione sucursal y cliente."); return; }
        Sucursal s = branches.get(selBranch);
        String dni = selClient.split(" - ")[0];
        Cliente c = s.getClientesAsignados().get(dni);
        String divisa = JOptionPane.showInputDialog(this, "Divisa (EUR):", "EUR");
        if (divisa == null) return;
        s.crearCuentaBancaria(c, divisa);
        CuentaBancaria acc = s.getCuentasAsociadas().get(s.getCuentasAsociadas().size()-1);
        accountsModel.addElement(acc.getNumeroCuenta() + " (" + acc.getDivisa() + ") -> " + acc.getSaldo());
        log.append("Cuenta creada para " + dni + ": " + acc.getNumeroCuenta() + "\n");
    }

    private void depositToSelectedAccount(double amount) {
        String sel = lstAccounts.getSelectedValue();
        if (sel == null) { JOptionPane.showMessageDialog(this, "Seleccione una cuenta."); return; }
        String iban = sel.split(" ")[0];
        // find account across branches
        CuentaBancaria target = null;
        for (Sucursal s : branches.values()) {
            for (CuentaBancaria c : s.getCuentasAsociadas()) {
                if (c.getNumeroCuenta().equals(iban)) { target = c; break; }
            }
            if (target != null) break;
        }
        if (target == null) return;
        target.setSaldo(target.getSaldo() + amount);
        onBranchSelected();
        log.append("Depositados " + amount + " en " + iban + "\n");
    }

    private void transferBetweenAccounts(double amount) {
        // choose origin and destination from accounts list via dialogs
        String sel = lstAccounts.getSelectedValue();
        if (sel == null) { JOptionPane.showMessageDialog(this, "Seleccione la cuenta origen en la lista de cuentas."); return; }
        String originIban = sel.split(" ")[0];
        String destIban = JOptionPane.showInputDialog(this, "IBAN destino:");
        if (destIban == null || destIban.trim().isEmpty()) return;
        CuentaBancaria origen = findAccountByIban(originIban);
        CuentaBancaria destino = findAccountByIban(destIban.trim());
        if (origen == null || destino == null) { JOptionPane.showMessageDialog(this, "No se encontraron una o ambas cuentas."); return; }
        boolean ok = origen.realizarTransferencia(destino, amount);
        if (ok) {
            // register in bank (find bank)
            EntidadBancaria eb = findBankForAccount(origen);
            if (eb != null) eb.registrarTransaccion(new Transaccion(origen, destino, amount));
            onBranchSelected();
            log.append("Transferencia " + amount + " de " + originIban + " a " + destIban + " OK\n");
        } else {
            log.append("Transferencia fallida: fondos insuficientes o cuenta inactiva.\n");
        }
    }

    // Helpers
    private CuentaBancaria findAccountByIban(String iban) {
        for (Sucursal s : branches.values()) {
            for (CuentaBancaria c : s.getCuentasAsociadas()) if (c.getNumeroCuenta().equals(iban)) return c;
        }
        // search in banks as well
        for (EntidadBancaria b : banks.values()) {
            try { return b.getCuenta(iban); } catch (Exception ex) {}
        }
        return null;
    }

    private EntidadBancaria findBankForAccount(CuentaBancaria cuenta) {
        for (EntidadBancaria b : banks.values()) {
            if (b.getCuentas().containsKey(cuenta.getNumeroCuenta())) return b;
        }
        return null;
    }
}
