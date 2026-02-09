
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Clase principal de la interfaz gráfica de usuario para CivisGeo.
 * Gestiona la navegación por pestañas y las operaciones principales.
 */

public class GuiMain extends JFrame {
    private DBManager db;
    private JTextArea logArea;
    private JLabel lblTotalPaises, lblTotalDivisiones, lblTotalProvincias, lblTotalLocalidades, lblTotalHabitantes,
            lblTotalCapitales, lblTotalCalles;

    // Components for Consultas tab
    private JComboBox<String> comboPais;
    private JComboBox<String> comboDivision;
    private JComboBox<String> comboProvincia;
    private JTextField txtBuscar;
    private JTable tablaLocalidades;
    private DefaultTableModel tableModel;

    public GuiMain() {
        db = new DBManager();
        setTitle("CivisGeo - Sistema de Gestión Territorial");
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main Container
        JPanel contentPane = new JPanel(new BorderLayout());
        setContentPane(contentPane);

        // Tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        contentPane.add(tabbedPane, BorderLayout.CENTER);

        // Tab 1: Dashboard
        tabbedPane.addTab("Dashboard", createDashboardPanel());

        // Tab 2: Consultas
        tabbedPane.addTab("Consultas", createConsultasPanel());

        // Tab 3: Importación
        tabbedPane.addTab("Importación", createImportacionPanel());

        // Tab 4: Admin
        tabbedPane.addTab("Admin", createAdminPanel());

        // Tab 5: Economy module
        try {
            tabbedPane.addTab("Economy", new EconomyPanel());
        } catch (Throwable t) {
            // If economy module missing or fails, show placeholder
            JPanel p = new JPanel(new BorderLayout());
            p.add(new JLabel("Economy module unavailable"), BorderLayout.CENTER);
            tabbedPane.addTab("Economy", p);
        }

        // Initial Data Load
        refreshStats();
        loadPaises();
    }

    // --- DASHBOARD ---
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 4, 20, 20)); // Grid 2 rows, 4 cols (updated)
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));

        lblTotalPaises = createStatCard(panel, "Países", "0", new Color(66, 133, 244));
        lblTotalDivisiones = createStatCard(panel, "Divisiones", "0", new Color(219, 68, 55));
        lblTotalProvincias = createStatCard(panel, "Provincias", "0", new Color(244, 180, 0));
        lblTotalLocalidades = createStatCard(panel, "Localidades", "0", new Color(15, 157, 88));
        lblTotalHabitantes = createStatCard(panel, "Habitantes", "0", new Color(103, 58, 183));
        lblTotalCapitales = createStatCard(panel, "Capitales", "0", new Color(255, 152, 0));
        lblTotalCalles = createStatCard(panel, "Calles Importadas", "0", new Color(96, 125, 139)); // Nuevo

        // Add placeholder empty card for layout balance
        // createStatCard(panel, "", "", Color.WHITE);

        return panel;
    }

    private JLabel createStatCard(JPanel parent, String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        card.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(Color.GRAY);
        lblTitle.setBorder(new EmptyBorder(10, 10, 0, 10));

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 36));
        lblValue.setForeground(color);
        lblValue.setHorizontalAlignment(SwingConstants.CENTER);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);

        parent.add(card);
        return lblValue; // Return the value label so we can update it
    }

    // --- CONSULTAS ---
    private JPanel createConsultasPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- Top Control Panel (Search + Filters) ---
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));

        // 1. Search Bar
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Buscar Localidad"));

        txtBuscar = new JTextField(20);
        JButton btnBuscar = new JButton("Buscar");
        btnBuscar.setIcon(UIManager.getIcon("FileView.fileIcon")); // Simple icon if available or text

        btnBuscar.addActionListener(e -> buscarLocalidades());
        txtBuscar.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    buscarLocalidades();
                }
            }
        });

        searchPanel.add(new JLabel("Nombre:"));
        searchPanel.add(txtBuscar);
        searchPanel.add(btnBuscar);

        // 2. Filters Bar
        JPanel filtersPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filtersPanel.setBorder(BorderFactory.createTitledBorder("Filtrar por Jerarquía"));

        comboPais = new JComboBox<>();
        comboPais.setPreferredSize(new Dimension(150, 30));
        comboPais.addActionListener(e -> onPaisSelected());

        comboDivision = new JComboBox<>();
        comboDivision.setPreferredSize(new Dimension(150, 30));
        comboDivision.addActionListener(e -> onDivisionSelected());

        comboProvincia = new JComboBox<>();
        comboProvincia.setPreferredSize(new Dimension(150, 30));
        comboProvincia.addActionListener(e -> onProvinciaSelected());

        JButton btnReset = new JButton("Limpiar Filtros");
        btnReset.addActionListener(e -> {
            loadPaises();
            // Limpia columnas y tabla
            tableModel.setColumnIdentifiers(
                    new String[] { "Localidad", "Tipo (Capital)", "Habitantes", "Código Postal", "Provincia" });
            tableModel.setRowCount(0);

            // Re-adjust column widths
            if (tablaLocalidades.getColumnCount() == 5) {
                tablaLocalidades.getColumnModel().getColumn(0).setPreferredWidth(200); // Localidad
                tablaLocalidades.getColumnModel().getColumn(1).setPreferredWidth(200); // Tipo (Capital)
                tablaLocalidades.getColumnModel().getColumn(2).setPreferredWidth(80); // Habitantes
                tablaLocalidades.getColumnModel().getColumn(3).setPreferredWidth(80); // CP
                tablaLocalidades.getColumnModel().getColumn(4).setPreferredWidth(150); // Provincia
            }

            txtBuscar.setText("");
        });

        filtersPanel.add(new JLabel("País:"));
        filtersPanel.add(comboPais);
        filtersPanel.add(new JLabel("División:"));
        filtersPanel.add(comboDivision);
        filtersPanel.add(new JLabel("Provincia:"));
        filtersPanel.add(comboProvincia);
        filtersPanel.add(btnReset);

        topPanel.add(searchPanel, BorderLayout.NORTH);
        topPanel.add(filtersPanel, BorderLayout.CENTER);

        panel.add(topPanel, BorderLayout.NORTH);

        // Table
        String[] columnNames = { "Localidad", "Tipo (Capital)", "Habitantes", "Código Postal", "Provincia" };
        tableModel = new DefaultTableModel(columnNames, 0);
        tablaLocalidades = new JTable(tableModel);
        // Ajustar anchos de columnas iniciales
        tablaLocalidades.setRowHeight(25);
        tablaLocalidades.getColumnModel().getColumn(0).setPreferredWidth(200); // Localidad
        tablaLocalidades.getColumnModel().getColumn(1).setPreferredWidth(200); // Tipo (Capital)
        tablaLocalidades.getColumnModel().getColumn(2).setPreferredWidth(80); // Habitantes
        tablaLocalidades.getColumnModel().getColumn(3).setPreferredWidth(80); // CP
        tablaLocalidades.getColumnModel().getColumn(4).setPreferredWidth(150); // Provincia

        JScrollPane scrollPane = new JScrollPane(tablaLocalidades);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // --- IMPORTACIÓN ---
    private JPanel createImportacionPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel buttonsPanel = new JPanel(new GridLayout(7, 1, 10, 10)); // Updated rows

        JButton btnImport1 = new JButton("1. Importar Países");
        btnImport1.addActionListener(e -> runImportTask("paises"));

        JButton btnImport2 = new JButton("2. Importar Divisiones");
        btnImport2.addActionListener(e -> runImportTask("divisiones"));

        JButton btnImport3 = new JButton("3. Importar Provincias");
        btnImport3.addActionListener(e -> runImportTask("provincias"));

        JButton btnImport4 = new JButton("4. Importar Localidades");
        btnImport4.addActionListener(e -> runImportTask("localidades"));

        JButton btnImportCap = new JButton("5. Importar Capitales (info)");
        btnImportCap.addActionListener(e -> runImportTask("capitales"));

        JButton btnImportCensus = new JButton("6. IMPORTAR CALLES (census.db)");
        btnImportCensus.setBackground(new Color(255, 224, 178));
        btnImportCensus.addActionListener(e -> runImportTask("census"));

        JButton btnImportAll = new JButton("IMPORTAR TODO (SECUENCIAL)");
        btnImportAll.setBackground(new Color(200, 230, 201));
        btnImportAll.addActionListener(e -> runImportTask("all"));

        buttonsPanel.add(btnImport1);
        buttonsPanel.add(btnImport2);
        buttonsPanel.add(btnImport3);
        buttonsPanel.add(btnImport4);
        buttonsPanel.add(btnImportCap);
        buttonsPanel.add(btnImportCensus);
        buttonsPanel.add(btnImportAll);

        panel.add(buttonsPanel, BorderLayout.WEST);

        // Log Area
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane logScroll = new JScrollPane(logArea);
        panel.add(logScroll, BorderLayout.CENTER);

        return panel;
    }

    // --- ADMIN ---
    private JPanel createAdminPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER)); // Cambiar a Layout más simple para centrar
        panel.setBorder(new EmptyBorder(50, 50, 50, 50));

        JButton btnClean = new JButton("LIMPIAR BASE DE DATOS");
        btnClean.setBackground(Color.RED);
        btnClean.setForeground(Color.BLACK);
        btnClean.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnClean.setPreferredSize(new Dimension(250, 50));

        btnClean.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "¿Está seguro de que desea BORRAR TODA la base de datos?\nEsta acción no se puede deshacer.",
                    "Confirmar Limpieza", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                db.limpiarBaseDatos();
                logArea.append("Base de datos limpiada.\n");
                refreshStats();
                loadPaises();
                JOptionPane.showMessageDialog(this, "Base de datos vaciada correctamente.");
            }
        });

        panel.add(btnClean);
        return panel;
    }

    // --- LOGIC ---

    private void refreshStats() {
        Map<String, Integer> stats = db.obtenerEstadisticasMap();
        lblTotalPaises.setText(String.valueOf(stats.getOrDefault("Paises", 0)));
        lblTotalDivisiones.setText(String.valueOf(stats.getOrDefault("Divisiones", 0)));
        lblTotalProvincias.setText(String.valueOf(stats.getOrDefault("Provincias", 0)));
        lblTotalLocalidades.setText(String.valueOf(stats.getOrDefault("Localidades", 0)));
        lblTotalHabitantes.setText(String.format("%,d", stats.getOrDefault("Habitantes", 0)));
        lblTotalCapitales.setText(String.valueOf(stats.getOrDefault("Capitales", 0)));
        lblTotalCalles.setText(String.valueOf(stats.getOrDefault("Calles", 0)));

        // Cargar principales localidades al inicio
        consultarPrincipalesLocalidades();
    }

    private void consultarPrincipalesLocalidades() {
        List<String[]> localidades = db.obtenerLocalidades();
        updateTableForLocalities(localidades);
    }

    private void loadPaises() {
        comboPais.removeAllItems();
        comboDivision.removeAllItems();
        comboProvincia.removeAllItems();
        tableModel.setRowCount(0);

        comboPais.addItem("Seleccione País...");
        List<String> paises = db.obtenerPaises();
        for (String p : paises) {
            comboPais.addItem(p);
        }
    }

    private void onPaisSelected() {
        if (comboPais.getSelectedItem() == null || comboPais.getSelectedIndex() == 0)
            return;

        String pais = (String) comboPais.getSelectedItem();
        comboDivision.removeAllItems();
        comboProvincia.removeAllItems();
        tableModel.setRowCount(0);

        comboDivision.addItem("Seleccione División...");

        // NUEVO: Obtener datos estructurados de divisiones
        List<String[]> divisionesData = db.obtenerDivisiones(pais);

        // 1. Mostrar en combo
        for (String[] div : divisionesData) {
            comboDivision.addItem(div[0]); // nombre
        }

        // 2. Mostrar en tabla
        updateTableForDivisions(divisionesData);
    }

    private void onDivisionSelected() {
        if (comboDivision.getSelectedItem() == null || comboDivision.getSelectedIndex() == 0)
            return;

        String division = (String) comboDivision.getSelectedItem();
        comboProvincia.removeAllItems();
        tableModel.setRowCount(0);

        comboProvincia.addItem("Seleccione Provincia...");

        // NUEVO: Obtener datos estructurados de provincias
        List<String[]> provinciasData = db.obtenerProvincias(division);

        for (String[] prov : provinciasData) {
            comboProvincia.addItem(prov[0]);
        }

        // Mostrar en tabla
        updateTableForProvincias(provinciasData);
    }

    private void onProvinciaSelected() {
        if (comboProvincia.getSelectedItem() == null || comboProvincia.getSelectedIndex() == 0)
            return;

        String provincia = (String) comboProvincia.getSelectedItem();
        List<String[]> localidades = db.obtenerLocalidadesDeProvincia(provincia);
        updateTableForLocalities(localidades);
    }

    private void buscarLocalidades() {
        String texto = txtBuscar.getText().trim();
        if (texto.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese un nombre para buscar.");
            return;
        }

        // Limpiar filtros para evitar confusión
        comboPais.setSelectedIndex(0);

        List<String[]> resultados = db.buscarLocalidades(texto);
        updateTableForLocalities(resultados);

        if (resultados.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No se encontraron localidades con ese nombre.");
        }
    }

    // --- TABLE UPDATERS ---

    // Actualiza tabla para mostrar Divisiones
    private void updateTableForDivisions(List<String[]> data) {
        // [Nombre, Tipo, Pais]
        tableModel.setColumnIdentifiers(new String[] { "División", "Tipo", "País" });
        tableModel.setRowCount(0);
        for (String[] div : data) {
            tableModel.addRow(div); // coincide con el orden
        }
    }

    // Actualiza tabla para mostrar Provincias
    private void updateTableForProvincias(List<String[]> data) {
        // [Nombre, Division, Pais]
        tableModel.setColumnIdentifiers(new String[] { "Provincia", "División", "País" });
        tableModel.setRowCount(0);
        for (String[] prov : data) {
            tableModel.addRow(prov);
        }
    }

    // Actualiza tabla para mostrar Localidades (formato estándar)
    private void updateTableForLocalities(List<String[]> data) {
        // [Nombre, Hab, Prov, CP, Capital]
        // Target: Localidad, Tipo (Capital), Habitantes, Código Postal, Provincia
        tableModel.setColumnIdentifiers(
                new String[] { "Localidad", "Tipo (Capital)", "Habitantes", "Código Postal", "Provincia" });
        tableModel.setRowCount(0);

        // Ajustar anchos de columnas
        if (tablaLocalidades.getColumnCount() == 5) {
            tablaLocalidades.getColumnModel().getColumn(0).setPreferredWidth(200); // Localidad
            tablaLocalidades.getColumnModel().getColumn(1).setPreferredWidth(200); // Tipo (Capital)
            tablaLocalidades.getColumnModel().getColumn(2).setPreferredWidth(80); // Habitantes
            tablaLocalidades.getColumnModel().getColumn(3).setPreferredWidth(80); // CP
            tablaLocalidades.getColumnModel().getColumn(4).setPreferredWidth(150); // Provincia
        }

        for (String[] row : data) {
            String nombre = row[0];
            String hab = row[1];
            String prov = row[2];
            String cp = row[3];
            String capital = row[4];

            tableModel.addRow(new Object[] { nombre, capital, hab, cp, prov });
        }
    }

    private void runImportTask(String type) {
        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                publish("Iniciando tarea: " + type + "...");
                long start = System.currentTimeMillis();

                if (type.equals("paises") || type.equals("all")) {
                    publish("Importando países...");
                    int c = db.importarPaisesCSV("csv/paises.csv");
                    publish("Países importados: " + c);
                }
                if (type.equals("divisiones") || type.equals("all")) {
                    publish("Importando divisiones...");
                    int c = db.importarDivisionesCSV("csv/divisiones.csv");
                    publish("Divisiones importadas: " + c);
                }
                if (type.equals("provincias") || type.equals("all")) {
                    publish("Importando provincias...");
                    int c = db.importarProvinciasCSV("csv/provincias.csv");
                    publish("Provincias importadas: " + c);
                }
                if (type.equals("localidades") || type.equals("all")) {
                    publish("Importando localidades...");
                    int c = db.importarLocalidadesCSV("csv/localidades.csv");
                    publish("Localidades importadas: " + c);
                }
                if (type.equals("capitales") || type.equals("all")) {
                    publish("Importando información de capitales...");
                    int c = db.importarCapitalesCSV("csv/capitales.csv");
                    publish("Capitales actualizadas: " + c);
                }
                if (type.equals("census") || type.equals("all")) {
                    // Check if census.db exists
                    File f = new File("census.db");
                    if (f.exists()) {
                        publish("Importando CALLES desde census.db (esto puede tardar)...");
                        db.importarDatosCenso(f.getAbsolutePath());
                        publish("Importación de calles finalizada.");
                    } else {
                        publish("ERROR: No se encontró census.db en la raíz.");
                    }
                }

                long time = System.currentTimeMillis() - start;
                publish("Tarea finalizada en " + time + " ms.");
                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                for (String msg : chunks) {
                    logArea.append(msg + "\n");
                }
            }

            @Override
            protected void done() {
                refreshStats();
                loadPaises();
                JOptionPane.showMessageDialog(GuiMain.this, "Operación Completada");
            }
        };
        worker.execute();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }

        SwingUtilities.invokeLater(() -> {
            new GuiMain().setVisible(true);
        });
    }
}
