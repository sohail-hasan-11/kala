package com.sms.view;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.FlatLaf;
import com.sms.dao.StudentDAO;
import com.sms.model.Student;
import com.sms.util.CSVExporter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Premium, fully-featured UI Main Frame for the Student Management System.
 * Handles Live Light/Dark Theme Switcher and Admin vs User Role Permissions.
 */
public class MainFrame extends JFrame {

    private final StudentDAO studentDAO;
    private final String userRole;      // "ADMIN" or "USER"
    private final String sessionUser;   // Logged in username
    private boolean isDarkMode = true;

    // Sidebar panels
    private CardLayout cardLayout;
    private JPanel contentPanel;

    // Student Directory UI elements
    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> tableSorter;

    private JTextField idField; // Hidden DB record ID
    private JTextField studentIdField;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JComboBox<String> genderCombo;
    private JComboBox<String> courseCombo;
    private JTextField gpaField;

    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton clearButton;
    private JButton importBtn; // Bulk import control

    // Filter controls
    private JTextField searchField;
    private JComboBox<String> filterCourseCombo;
    private JComboBox<String> filterStandingCombo;

    // Dashboard UI elements
    private JLabel totalStudentsLabel;
    private JLabel avgGpaLabel;
    private JLabel genderRatioLabel;
    private JLabel topMajorLabel;
    private GpaChartPanel gpaChartPanel;

    // Status components
    private JLabel statusLabel;
    private JLabel countLabel;

    public MainFrame(String role, String username) {
        this.studentDAO = new StudentDAO();
        this.userRole = role;
        this.sessionUser = username;

        setTitle("Academix - Enterprise Student Portal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1250, 780);
        setMinimumSize(new Dimension(1100, 680));
        setLocationRelativeTo(null);

        initUI();
        applyRolePermissions();
        checkDbAndLoadData();
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        setContentPane(mainPanel);

        // 1. Sidebar Navigation (Left)
        mainPanel.add(createSidebarPanel(), BorderLayout.WEST);

        // 2. Main Content panel (Center)
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        contentPanel.add(createDashboardPanel(), "DASHBOARD");
        contentPanel.add(createDirectoryPanel(), "DIRECTORY");
        contentPanel.add(createDataToolsPanel(), "TOOLS");

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // 3. Bottom Status Bar
        mainPanel.add(createStatusBar(), BorderLayout.SOUTH);
    }

    private void applyRolePermissions() {
        if ("USER".equalsIgnoreCase(userRole)) {
            // Disable CRUD operations on the directory panel
            addButton.setEnabled(false);
            updateButton.setEnabled(false);
            deleteButton.setEnabled(false);
            clearButton.setEnabled(false);

            // Make text fields un-editable to Viewer/User role
            studentIdField.setEditable(false);
            firstNameField.setEditable(false);
            lastNameField.setEditable(false);
            emailField.setEditable(false);
            phoneField.setEditable(false);
            genderCombo.setEnabled(false);
            courseCombo.setEnabled(false);
            gpaField.setEditable(false);

            // Disable Tools Bulk Import button
            importBtn.setEnabled(false);
            importBtn.setToolTipText("Administrators only");
        }
    }

    /**
     * Left Sidebar Navigation Panel.
     */
    private JPanel createSidebarPanel() {
        JPanel sidebar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isDarkMode) {
                    g2d.setPaint(new GradientPaint(0, 0, new Color(24, 28, 36), 0, getHeight(), new Color(34, 40, 52)));
                } else {
                    g2d.setPaint(new GradientPaint(0, 0, new Color(225, 230, 240), 0, getHeight(), new Color(240, 244, 250)));
                }
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(200, 205, 215)));

        // Profile / Brand Section
        JLabel brandLabel = new JLabel("🎓 ACADEMIX");
        brandLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        brandLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        brandLabel.setBorder(new EmptyBorder(30, 0, 5, 0));
        sidebar.add(brandLabel);

        JLabel subBrandLabel = new JLabel("Portal Administration");
        subBrandLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subBrandLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subBrandLabel.setBorder(new EmptyBorder(0, 0, 30, 0));
        sidebar.add(subBrandLabel);

        sidebar.add(Box.createVerticalStrut(10));

        // Navigation Buttons
        JButton dashBtn = createNavButton("📊   Dashboard", "DASHBOARD");
        JButton dirBtn = createNavButton("📂   Student Directory", "DIRECTORY");
        JButton toolsBtn = createNavButton("⚙️   Data & Import Tools", "TOOLS");

        sidebar.add(dashBtn);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(dirBtn);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(toolsBtn);

        // Sidebar glue pushes theme switch to bottom
        sidebar.add(Box.createVerticalGlue());

        // Theme Switch Toggle
        JButton themeBtn = new JButton("🌓 Switch Theme");
        themeBtn.setMaximumSize(new Dimension(210, 35));
        themeBtn.setPreferredSize(new Dimension(210, 35));
        themeBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        themeBtn.setFocusPainted(false);
        themeBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        themeBtn.putClientProperty("JComponent.roundRect", true);
        themeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        themeBtn.addActionListener(e -> toggleTheme());
        sidebar.add(themeBtn);

        sidebar.add(Box.createVerticalStrut(15));

        JLabel versionLabel = new JLabel("v2.2.0 Stable");
        versionLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        versionLabel.setForeground(new Color(130, 140, 155));
        versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        versionLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        sidebar.add(versionLabel);

        return sidebar;
    }

    private JButton createNavButton(String text, String cardName) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(210, 42));
        btn.setPreferredSize(new Dimension(210, 42));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(0, 15, 0, 0));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.putClientProperty("JComponent.roundRect", true);
        btn.addActionListener(e -> cardLayout.show(contentPanel, cardName));

        btn.setBackground(new Color(60, 65, 80, 40));

        return btn;
    }

    private void toggleTheme() {
        try {
            if (isDarkMode) {
                UIManager.setLookAndFeel(new FlatLightLaf());
                isDarkMode = false;
            } else {
                UIManager.setLookAndFeel(new FlatDarkLaf());
                isDarkMode = true;
            }
            FlatLaf.updateUI();
            this.repaint();
        } catch (Exception ex) {
            System.err.println("Failed to switch theme.");
        }
    }

    /**
     * Dashboard Panel View.
     */
    private JPanel createDashboardPanel() {
        JPanel dashboard = new JPanel(new BorderLayout(15, 15));
        dashboard.setOpaque(false);

        // Header Title
        JLabel head = new JLabel("📊 Analytics Dashboard");
        head.setFont(new Font("Segoe UI", Font.BOLD, 22));
        dashboard.add(head, BorderLayout.NORTH);

        // Top Cards layout (Grid of Stats - 4 Columns)
        JPanel cardsGrid = new JPanel(new GridLayout(1, 4, 15, 0));
        cardsGrid.setOpaque(false);

        totalStudentsLabel = new JLabel("0");
        avgGpaLabel = new JLabel("0.00");
        genderRatioLabel = new JLabel("0 M / 0 F");
        topMajorLabel = new JLabel("N/A");

        cardsGrid.add(createStatCard("TOTAL STUDENTS REGISTERED", totalStudentsLabel, "👥", new Color(41, 128, 185)));
        cardsGrid.add(createStatCard("AVERAGE PORTAL GPA Score", avgGpaLabel, "📈", new Color(39, 174, 96)));
        cardsGrid.add(createStatCard("GENDER DEMOGRAPHIC RATIO", genderRatioLabel, "⚖️", new Color(142, 68, 173)));
        cardsGrid.add(createStatCard("TOP PERFORMING COURSE", topMajorLabel, "🏆", new Color(230, 126, 34)));
        dashboard.add(cardsGrid, BorderLayout.CENTER);

        // Bottom Chart Layout
        JPanel chartContainer = new JPanel(new BorderLayout(10, 10));
        chartContainer.setPreferredSize(new Dimension(0, 380));
        chartContainer.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(100, 100, 100, 50), 1, true),
                new EmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel chartTitle = new JLabel("Academic Performance Distribution (GPA Categories)");
        chartTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        chartContainer.add(chartTitle, BorderLayout.NORTH);

        gpaChartPanel = new GpaChartPanel();
        chartContainer.add(gpaChartPanel, BorderLayout.CENTER);

        dashboard.add(chartContainer, BorderLayout.SOUTH);

        return dashboard;
    }

    private JPanel createStatCard(String headerText, JLabel valueLabel, String emoji, Color accentColor) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isDarkMode) {
                    g2.setColor(new Color(40, 44, 52));
                } else {
                    g2.setColor(new Color(245, 247, 250));
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(accentColor);
                g2.fillRoundRect(0, 0, 8, getHeight(), 12, 12);
            }
        };
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(15, 20, 15, 15));
        card.setOpaque(false);

        JLabel head = new JLabel(headerText);
        head.setFont(new Font("Segoe UI", Font.BOLD, 10));
        head.setForeground(new Color(130, 140, 155));
        card.add(head, BorderLayout.NORTH);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        card.add(valueLabel, BorderLayout.CENTER);

        JLabel icon = new JLabel(emoji);
        icon.setFont(new Font("Segoe UI", Font.PLAIN, 28));
        card.add(icon, BorderLayout.EAST);

        return card;
    }

    /**
     * Directory Panel (List and Forms).
     */
    private JPanel createDirectoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setOpaque(false);

        // Header Top Row
        JPanel topRow = new JPanel(new BorderLayout(10, 0));
        topRow.setOpaque(false);
        
        JLabel title = new JLabel("👥 Student Directory");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        topRow.add(title, BorderLayout.WEST);

        JPanel actionWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionWrap.setOpaque(false);

        JLabel filterLbl = new JLabel("Filters:");
        filterLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        actionWrap.add(filterLbl);

        filterCourseCombo = new JComboBox<>(new String[]{
                "All Courses", "Computer Science", "Information Technology", "Software Engineering",
                "Data Science", "Electrical Engineering", "Mechanical Engineering",
                "Business Administration", "Mathematics"
        });
        filterCourseCombo.putClientProperty("JComponent.roundRect", true);
        filterCourseCombo.addActionListener(e -> applyFilters());
        actionWrap.add(filterCourseCombo);

        filterStandingCombo = new JComboBox<>(new String[]{
                "All Standings", "Dean's List (3.5+)", "Good Standing (2.0-3.5)", "Academic Warning (<2.0)"
        });
        filterStandingCombo.putClientProperty("JComponent.roundRect", true);
        filterStandingCombo.addActionListener(e -> applyFilters());
        actionWrap.add(filterStandingCombo);

        searchField = new JTextField(15);
        searchField.putClientProperty("JTextField.placeholderText", "Search ID, Name...");
        searchField.putClientProperty("JTextField.showClearButton", true);
        searchField.putClientProperty("JComponent.roundRect", true);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applyFilters(); }
            @Override public void removeUpdate(DocumentEvent e) { applyFilters(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });
        actionWrap.add(searchField);

        JButton printCardBtn = new JButton("🖨️ Report Card");
        printCardBtn.putClientProperty("JComponent.roundRect", true);
        printCardBtn.setBackground(new Color(142, 68, 173));
        printCardBtn.setForeground(Color.WHITE);
        printCardBtn.addActionListener(e -> showReportCardDialog());
        actionWrap.add(printCardBtn);

        topRow.add(actionWrap, BorderLayout.EAST);
        panel.add(topRow, BorderLayout.NORTH);

        // Left Form
        JPanel formContainer = new JPanel(new BorderLayout());
        formContainer.setPreferredSize(new Dimension(350, 0));
        formContainer.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(120, 120, 120, 40), 1, true),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        TitledBorder titledBorder = BorderFactory.createTitledBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(120, 120, 120, 80)),
                " STUDENT REGISTRATION ", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12), new Color(150, 180, 220)
        );
        fieldsPanel.setBorder(titledBorder);

        int row = 0;
        idField = new JTextField(); // Hidden DB representation

        addFormLabel("Student ID *:", fieldsPanel, gbc, row);
        studentIdField = new JTextField();
        studentIdField.putClientProperty("JTextField.placeholderText", "e.g. STU-1001");
        applyFieldStyle(studentIdField);
        addFormField(studentIdField, fieldsPanel, gbc, row++);

        addFormLabel("First Name *:", fieldsPanel, gbc, row);
        firstNameField = new JTextField();
        applyFieldStyle(firstNameField);
        addFormField(firstNameField, fieldsPanel, gbc, row++);

        addFormLabel("Last Name *:", fieldsPanel, gbc, row);
        lastNameField = new JTextField();
        applyFieldStyle(lastNameField);
        addFormField(lastNameField, fieldsPanel, gbc, row++);

        addFormLabel("Email Address *:", fieldsPanel, gbc, row);
        emailField = new JTextField();
        emailField.putClientProperty("JTextField.placeholderText", "name@example.com");
        applyFieldStyle(emailField);
        addFormField(emailField, fieldsPanel, gbc, row++);

        addFormLabel("Phone Number:", fieldsPanel, gbc, row);
        phoneField = new JTextField();
        applyFieldStyle(phoneField);
        addFormField(phoneField, fieldsPanel, gbc, row++);

        addFormLabel("Gender:", fieldsPanel, gbc, row);
        genderCombo = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        genderCombo.putClientProperty("JComponent.roundRect", true);
        addFormField(genderCombo, fieldsPanel, gbc, row++);

        addFormLabel("Course / Major:", fieldsPanel, gbc, row);
        courseCombo = new JComboBox<>(new String[]{
                "Computer Science", "Information Technology", "Software Engineering",
                "Data Science", "Electrical Engineering", "Mechanical Engineering",
                "Business Administration", "Mathematics"
        });
        courseCombo.putClientProperty("JComponent.roundRect", true);
        addFormField(courseCombo, fieldsPanel, gbc, row++);

        addFormLabel("GPA Metric *:", fieldsPanel, gbc, row);
        gpaField = new JTextField();
        gpaField.putClientProperty("JTextField.placeholderText", "e.g. 3.80");
        applyFieldStyle(gpaField);
        addFormField(gpaField, fieldsPanel, gbc, row++);

        formContainer.add(fieldsPanel, BorderLayout.CENTER);

        // Buttons Panel
        JPanel buttonsPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        buttonsPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        addButton = new JButton("➕ Register");
        addButton.setBackground(new Color(41, 128, 185));
        addButton.setForeground(Color.WHITE);
        styleButton(addButton);
        addButton.addActionListener(e -> addStudent());

        updateButton = new JButton("✏️ Update");
        updateButton.setBackground(new Color(230, 126, 34));
        updateButton.setForeground(Color.WHITE);
        styleButton(updateButton);
        updateButton.setEnabled(false);
        updateButton.addActionListener(e -> updateStudent());

        deleteButton = new JButton("🗑️ Deregister");
        deleteButton.setBackground(new Color(192, 57, 43));
        deleteButton.setForeground(Color.WHITE);
        styleButton(deleteButton);
        deleteButton.setEnabled(false);
        deleteButton.addActionListener(e -> deleteStudent());

        clearButton = new JButton("🧹 Clear");
        styleButton(clearButton);
        clearButton.addActionListener(e -> clearFields());

        buttonsPanel.add(addButton);
        buttonsPanel.add(updateButton);
        buttonsPanel.add(deleteButton);
        buttonsPanel.add(clearButton);

        formContainer.add(buttonsPanel, BorderLayout.SOUTH);
        panel.add(formContainer, BorderLayout.WEST);

        // Table Panel (Right)
        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setBorder(new LineBorder(new Color(120, 120, 120, 40), 1, true));

        String[] columns = {"ID", "Student ID", "First Name", "Last Name", "Email", "Phone", "Gender", "Course", "GPA"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        tableSorter = new TableRowSorter<>(tableModel);

        table = new JTable(tableModel);
        table.setRowSorter(tableSorter);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(28);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));

        // Hide ID
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);

        table.getColumnModel().getColumn(1).setPreferredWidth(80);
        table.getColumnModel().getColumn(2).setPreferredWidth(90);
        table.getColumnModel().getColumn(3).setPreferredWidth(90);
        table.getColumnModel().getColumn(4).setPreferredWidth(160);
        table.getColumnModel().getColumn(5).setPreferredWidth(100);
        table.getColumnModel().getColumn(6).setPreferredWidth(65);
        table.getColumnModel().getColumn(7).setPreferredWidth(130);
        table.getColumnModel().getColumn(8).setPreferredWidth(50);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(8).setCellRenderer(centerRenderer);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int r = table.getSelectedRow();
                if (r != -1) populateFieldsFromSelection(r);
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        tableContainer.add(scrollPane, BorderLayout.CENTER);
        panel.add(tableContainer, BorderLayout.CENTER);

        return panel;
    }

    /**
     * CSV tools panel.
     */
    private JPanel createDataToolsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        panel.setOpaque(false);

        JPanel card1 = new JPanel(new BorderLayout(10, 10));
        card1.setPreferredSize(new Dimension(300, 155));
        card1.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(120, 120, 120, 40), 1, true),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel title1 = new JLabel("📤 Export Student Directory");
        title1.setFont(new Font("Segoe UI", Font.BOLD, 14));
        card1.add(title1, BorderLayout.NORTH);

        JLabel desc1 = new JLabel("<html>Download full student registration records directly into a standard format spreadsheet (.csv).</html>");
        desc1.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        desc1.setForeground(new Color(130, 140, 155));
        card1.add(desc1, BorderLayout.CENTER);

        JButton exportBtn = new JButton("Export to CSV");
        exportBtn.putClientProperty("JComponent.roundRect", true);
        exportBtn.setBackground(new Color(46, 204, 113));
        exportBtn.setForeground(Color.WHITE);
        exportBtn.addActionListener(e -> exportToCSV());
        card1.add(exportBtn, BorderLayout.SOUTH);

        JPanel card2 = new JPanel(new BorderLayout(10, 10));
        card2.setPreferredSize(new Dimension(300, 155));
        card2.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(120, 120, 120, 40), 1, true),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel title2 = new JLabel("📥 Bulk Import Students");
        title2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        card2.add(title2, BorderLayout.NORTH);

        JLabel desc2 = new JLabel("<html>Select a structured CSV file from your computer to insert student records in bulk to MySQL.</html>");
        desc2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        desc2.setForeground(new Color(130, 140, 155));
        card2.add(desc2, BorderLayout.CENTER);

        importBtn = new JButton("Import from CSV");
        importBtn.putClientProperty("JComponent.roundRect", true);
        importBtn.setBackground(new Color(52, 152, 219));
        importBtn.setForeground(Color.WHITE);
        importBtn.addActionListener(e -> importFromCSV());
        card2.add(importBtn, BorderLayout.SOUTH);

        panel.add(card1);
        panel.add(card2);

        return panel;
    }

    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout(10, 0));
        statusBar.setBorder(new EmptyBorder(6, 10, 6, 10));
        statusBar.setOpaque(false);

        statusLabel = new JLabel("Connecting to MySQL Database...");
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        statusBar.add(statusLabel, BorderLayout.WEST);

        countLabel = new JLabel("Total: 0 students registered");
        countLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusBar.add(countLabel, BorderLayout.EAST);

        return statusBar;
    }

    private void applyFieldStyle(JTextField field) {
        field.putClientProperty("JComponent.roundRect", true);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setMargin(new Insets(4, 8, 4, 8));
    }

    private void styleButton(JButton btn) {
        btn.putClientProperty("JComponent.roundRect", true);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void addFormLabel(String text, JPanel panel, GridBagConstraints gbc, int row) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(new Color(120, 130, 145));
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        panel.add(label, gbc);
    }

    private void addFormField(JComponent comp, JPanel panel, GridBagConstraints gbc, int row) {
        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 0.7;
        panel.add(comp, gbc);
    }

    // -------------------------------------------------------------------------
    // DYNAMIC FILTER IMPLEMENTATION
    // -------------------------------------------------------------------------

    private void applyFilters() {
        String searchText = searchField.getText().trim();
        String selectedCourse = (String) filterCourseCombo.getSelectedItem();
        int standingIndex = filterStandingCombo.getSelectedIndex();

        List<RowFilter<Object, Object>> filters = new ArrayList<>();

        if (!searchText.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + Pattern.quote(searchText)));
        }

        if (selectedCourse != null && !selectedCourse.equals("All Courses")) {
            filters.add(RowFilter.regexFilter("^" + Pattern.quote(selectedCourse) + "$", 7));
        }

        if (standingIndex > 0) {
            filters.add(new RowFilter<Object, Object>() {
                @Override
                public boolean include(Entry<? extends Object, ? extends Object> entry) {
                    try {
                        double gpa = Double.parseDouble(entry.getStringValue(8));
                        if (standingIndex == 1) return gpa >= 3.5;
                        if (standingIndex == 2) return gpa >= 2.0 && gpa < 3.5;
                        if (standingIndex == 3) return gpa < 2.0;
                    } catch (NumberFormatException e) {
                        // ignore
                    }
                    return false;
                }
            });
        }

        if (filters.isEmpty()) {
            tableSorter.setRowFilter(null);
        } else {
            tableSorter.setRowFilter(RowFilter.andFilter(filters));
        }
    }

    // -------------------------------------------------------------------------
    // DATA LOADERS & ADVISORY STATS
    // -------------------------------------------------------------------------

    private void checkDbAndLoadData() {
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                try (java.sql.Connection conn = com.sms.util.DBConnection.getConnection()) {
                    return conn != null;
                } catch (Exception e) {
                    return false;
                }
            }

            @Override
            protected void done() {
                try {
                    boolean connected = get();
                    if (connected) {
                        statusLabel.setText("● Logged in: " + sessionUser + " (" + userRole + ") | Connected to DB");
                        statusLabel.setForeground(new Color(46, 204, 113));
                        loadStudentData();
                    } else {
                        statusLabel.setText("▲ Connection Failed. Running Offline");
                        statusLabel.setForeground(new Color(231, 76, 60));
                        JOptionPane.showMessageDialog(MainFrame.this,
                                "Could not connect to database 'student_db'.\nPlease run 'sudo ./configure_mysql.sh' first.",
                                "Database offline", JOptionPane.WARNING_MESSAGE);
                    }
                } catch (Exception e) {
                    statusLabel.setText("▲ Error starting application");
                }
            }
        };
        worker.execute();
    }

    private void loadStudentData() {
        SwingWorker<List<Student>, Void> worker = new SwingWorker<>() {
            @Override protected List<Student> doInBackground() throws SQLException {
                return studentDAO.getAllStudents();
            }

            @Override protected void done() {
                try {
                    List<Student> list = get();
                    populateTable(list);
                    updateDashboard(list);
                } catch (Exception e) {
                    showError("Error loading student data", e);
                }
            }
        };
        worker.execute();
    }

    private void populateTable(List<Student> students) {
        tableModel.setRowCount(0);
        for (Student s : students) {
            tableModel.addRow(new Object[]{
                    s.getId(), s.getStudentId(), s.getFirstName(), s.getLastName(),
                    s.getEmail(), s.getPhone(), s.getGender(), s.getCourse(), s.getGpa()
            });
        }
        countLabel.setText("Total: " + students.size() + " students registered");
    }

    private void updateDashboard(List<Student> students) {
        int total = students.size();
        totalStudentsLabel.setText(String.valueOf(total));

        double sum = 0;
        int mCount = 0;
        int fCount = 0;

        int[] gpaCounts = new int[4]; // [3.5-4.0, 3.0-3.5, 2.5-3.0, <2.5]

        String[] courses = {
            "Computer Science", "Information Technology", "Software Engineering",
            "Data Science", "Electrical Engineering", "Mechanical Engineering",
            "Business Administration", "Mathematics"
        };
        double[] courseGpaSums = new double[courses.length];
        int[] courseCounts = new int[courses.length];

        for (Student s : students) {
            sum += s.getGpa();
            if ("Male".equalsIgnoreCase(s.getGender())) mCount++;
            else if ("Female".equalsIgnoreCase(s.getGender())) fCount++;

            double g = s.getGpa();
            if (g >= 3.5) gpaCounts[0]++;
            else if (g >= 3.0) gpaCounts[1]++;
            else if (g >= 2.5) gpaCounts[2]++;
            else gpaCounts[3]++;

            // Course mapping
            for (int i = 0; i < courses.length; i++) {
                if (courses[i].equalsIgnoreCase(s.getCourse())) {
                    courseGpaSums[i] += s.getGpa();
                    courseCounts[i]++;
                    break;
                }
            }
        }

        double avg = (total > 0) ? (sum / total) : 0.0;
        avgGpaLabel.setText(String.format("%.2f", avg));
        genderRatioLabel.setText(mCount + " M / " + fCount + " F");

        // Top Major calculation
        String topCourse = "N/A";
        double maxAvg = 0;
        for (int i = 0; i < courses.length; i++) {
            if (courseCounts[i] > 0) {
                double courseAvg = courseGpaSums[i] / courseCounts[i];
                if (courseAvg > maxAvg) {
                    maxAvg = courseAvg;
                    topCourse = courses[i];
                }
            }
        }
        if (maxAvg > 0) {
            topMajorLabel.setText(String.format("%s (%.2f)", getAbbr(topCourse), maxAvg));
        } else {
            topMajorLabel.setText("N/A");
        }

        gpaChartPanel.setCounts(gpaCounts);
    }

    private String getAbbr(String course) {
        if ("Computer Science".equalsIgnoreCase(course)) return "CS";
        if ("Information Technology".equalsIgnoreCase(course)) return "IT";
        if ("Software Engineering".equalsIgnoreCase(course)) return "SE";
        if ("Data Science".equalsIgnoreCase(course)) return "DS";
        if ("Business Administration".equalsIgnoreCase(course)) return "BBA";
        return course;
    }

    private void autoGenerateStudentId() {
        if ("USER".equalsIgnoreCase(userRole)) return; // No ID generation for read-only user
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                return studentDAO.getNextStudentId();
            }

            @Override
            protected void done() {
                try {
                    studentIdField.setText(get());
                } catch (Exception e) {
                    studentIdField.setText("STU-1001");
                }
            }
        };
        worker.execute();
    }

    // -------------------------------------------------------------------------
    // CRUD CONTROLLER
    // -------------------------------------------------------------------------

    private void populateFieldsFromSelection(int selectedRow) {
        int mr = table.convertRowIndexToModel(selectedRow);

        idField.setText(tableModel.getValueAt(mr, 0).toString());
        studentIdField.setText(tableModel.getValueAt(mr, 1).toString());
        firstNameField.setText(tableModel.getValueAt(mr, 2).toString());
        lastNameField.setText(tableModel.getValueAt(mr, 3).toString());
        emailField.setText(tableModel.getValueAt(mr, 4).toString());
        
        Object phoneVal = tableModel.getValueAt(mr, 5);
        phoneField.setText(phoneVal != null ? phoneVal.toString() : "");

        genderCombo.setSelectedItem(tableModel.getValueAt(mr, 6).toString());
        courseCombo.setSelectedItem(tableModel.getValueAt(mr, 7).toString());
        gpaField.setText(tableModel.getValueAt(mr, 8).toString());

        if ("ADMIN".equalsIgnoreCase(userRole)) {
            addButton.setEnabled(false);
            updateButton.setEnabled(true);
            deleteButton.setEnabled(true);
        }
    }

    private void clearFields() {
        if ("USER".equalsIgnoreCase(userRole)) return; // Viewers can't clear form
        idField.setText("");
        autoGenerateStudentId();
        firstNameField.setText("");
        lastNameField.setText("");
        emailField.setText("");
        phoneField.setText("");
        genderCombo.setSelectedIndex(0);
        courseCombo.setSelectedIndex(0);
        gpaField.setText("");

        table.clearSelection();
        addButton.setEnabled(true);
        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);
    }

    private void addStudent() {
        if (!validateFields()) return;

        Student student = new Student(
                studentIdField.getText().trim(),
                firstNameField.getText().trim(),
                lastNameField.getText().trim(),
                emailField.getText().trim(),
                phoneField.getText().trim(),
                genderCombo.getSelectedItem().toString(),
                courseCombo.getSelectedItem().toString(),
                Double.parseDouble(gpaField.getText().trim())
        );

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override protected Void doInBackground() throws SQLException {
                studentDAO.addStudent(student);
                return null;
            }
            @Override protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(MainFrame.this, "Student registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearFields();
                    loadStudentData();
                } catch (Exception e) {
                    showError("Could not add student", e);
                }
            }
        };
        worker.execute();
    }

    private void updateStudent() {
        if (idField.getText().isEmpty()) return;
        if (!validateFields()) return;

        Student student = new Student(
                Integer.parseInt(idField.getText()),
                studentIdField.getText().trim(),
                firstNameField.getText().trim(),
                lastNameField.getText().trim(),
                emailField.getText().trim(),
                phoneField.getText().trim(),
                genderCombo.getSelectedItem().toString(),
                courseCombo.getSelectedItem().toString(),
                Double.parseDouble(gpaField.getText().trim())
        );

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override protected Void doInBackground() throws SQLException {
                studentDAO.updateStudent(student);
                return null;
            }
            @Override protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(MainFrame.this, "Student details updated!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearFields();
                    loadStudentData();
                } catch (Exception e) {
                    showError("Could not update student", e);
                }
            }
        };
        worker.execute();
    }

    private void deleteStudent() {
        if (idField.getText().isEmpty()) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to deregister student " + firstNameField.getText() + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            int id = Integer.parseInt(idField.getText());
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override protected Void doInBackground() throws SQLException {
                    studentDAO.deleteStudent(id);
                    return null;
                }
                @Override protected void done() {
                    try {
                        get();
                        JOptionPane.showMessageDialog(MainFrame.this, "Student deregistered.", "Success", JOptionPane.INFORMATION_MESSAGE);
                        clearFields();
                        loadStudentData();
                    } catch (Exception e) {
                        showError("Could not delete student", e);
                    }
                }
            };
            worker.execute();
        }
    }

    private boolean validateFields() {
        if (studentIdField.getText().trim().isEmpty()) {
            showValidationError("Student ID is required."); return false;
        }
        if (firstNameField.getText().trim().isEmpty() || lastNameField.getText().trim().isEmpty()) {
            showValidationError("Full Student Name is required."); return false;
        }
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            showValidationError("Email address is required."); return false;
        }
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        if (!Pattern.compile(emailRegex).matcher(email).matches()) {
            showValidationError("Enter a valid email structure."); return false;
        }
        String gpaStr = gpaField.getText().trim();
        if (gpaStr.isEmpty()) {
            showValidationError("GPA score is required."); return false;
        }
        try {
            double g = Double.parseDouble(gpaStr);
            if (g < 0.0 || g > 4.0) {
                showValidationError("GPA must scale between 0.0 and 4.0."); return false;
            }
        } catch (NumberFormatException e) {
            showValidationError("GPA must be numerical."); return false;
        }
        return true;
    }

    private void showValidationError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Validation Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showError(String context, Exception e) {
        String message = e.getMessage();
        if (message != null && message.contains("Duplicate entry")) {
            if (message.contains("student_id")) {
                message = "The Student ID already exists. Please use a unique Student ID.";
            } else if (message.contains("email")) {
                message = "The Email address is already in use by another student.";
            }
        }
        JOptionPane.showMessageDialog(this, context + ":\n" + message, "Database Error", JOptionPane.ERROR_MESSAGE);
    }

    // -------------------------------------------------------------------------
    // FILE EXPORTS
    // -------------------------------------------------------------------------

    private void exportToCSV() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("student_directory.csv"));
        int ret = chooser.showSaveDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            File dest = chooser.getSelectedFile();
            try {
                CSVExporter.exportToCSV(table, dest);
                JOptionPane.showMessageDialog(this, "Data exported successfully to CSV!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                showError("Export failed", e);
            }
        }
    }

    private void importFromCSV() {
        JFileChooser chooser = new JFileChooser();
        int ret = chooser.showOpenDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            File csvFile = chooser.getSelectedFile();
            
            SwingWorker<Integer, Void> worker = new SwingWorker<>() {
                @Override
                protected Integer doInBackground() throws Exception {
                    List<Student> list = new ArrayList<>();
                    try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
                        String line = reader.readLine(); // Header line
                        while ((line = reader.readLine()) != null) {
                            String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                            if (data.length >= 8) {
                                String stuId = data[0].replace("\"", "");
                                String first = data[1].replace("\"", "");
                                String last = data[2].replace("\"", "");
                                String email = data[3].replace("\"", "");
                                String phone = data[4].replace("\"", "");
                                String gender = data[5].replace("\"", "");
                                String course = data[6].replace("\"", "");
                                double gpa = Double.parseDouble(data[7].replace("\"", "").trim());
                                list.add(new Student(stuId, first, last, email, phone, gender, course, gpa));
                            }
                        }
                    }
                    if (!list.isEmpty()) {
                        studentDAO.addStudentsBulk(list);
                    }
                    return list.size();
                }

                @Override
                protected void done() {
                    try {
                        int inserted = get();
                        JOptionPane.showMessageDialog(MainFrame.this, "Successfully imported " + inserted + " records into MySQL!", "Import Successful", JOptionPane.INFORMATION_MESSAGE);
                        loadStudentData();
                    } catch (Exception e) {
                        showError("Failed to parse or save CSV data", e);
                    }
                }
            };
            worker.execute();
        }
    }

    // -------------------------------------------------------------------------
    // OFFICIAL PRINT TRANSCRIPT DIALOG
    // -------------------------------------------------------------------------

    private void showReportCardDialog() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a student first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);
        String name = tableModel.getValueAt(modelRow, 2).toString() + " " + tableModel.getValueAt(modelRow, 3).toString();
        String id = tableModel.getValueAt(modelRow, 1).toString();
        String email = tableModel.getValueAt(modelRow, 4).toString();
        String course = tableModel.getValueAt(modelRow, 7).toString();
        double gpa = Double.parseDouble(tableModel.getValueAt(modelRow, 8).toString());

        String standing = "Good Standing";
        if (gpa >= 3.5) standing = "Excellent (Dean's List Honour)";
        else if (gpa >= 2.0 && gpa < 3.0) standing = "Satisfactory";
        else if (gpa < 2.0) standing = "Academic Warning Status";

        JDialog dialog = new JDialog(this, "Official Academic Transcript - " + id, true);
        dialog.setSize(450, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel docPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRect(10, 10, getWidth() - 20, getHeight() - 20);
                g2.setColor(new Color(44, 62, 80));
                g2.setStroke(new BasicStroke(3));
                g2.drawRect(15, 15, getWidth() - 30, getHeight() - 30);
            }
        };
        docPanel.setLayout(null);
        docPanel.setOpaque(false);

        JLabel h1 = new JLabel("ACADEMIX UNIVERSITY");
        h1.setFont(new Font("Georgia", Font.BOLD, 18));
        h1.setForeground(new Color(44, 62, 80));
        h1.setBounds(30, 30, 390, 25);
        h1.setHorizontalAlignment(SwingConstants.CENTER);
        docPanel.add(h1);

        JLabel h2 = new JLabel("OFFICIAL GRADE STATEMENT");
        h2.setFont(new Font("Segoe UI", Font.BOLD, 11));
        h2.setForeground(new Color(127, 140, 141));
        h2.setBounds(30, 55, 390, 15);
        h2.setHorizontalAlignment(SwingConstants.CENTER);
        docPanel.add(h2);

        JPanel div = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(new Color(189, 195, 199));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        div.setBounds(35, 75, 370, 2);
        docPanel.add(div);

        int startY = 100;
        int rowH = 30;

        addPrintLine("Student ID:", id, startY, docPanel);
        addPrintLine("Full Name:", name, startY + rowH, docPanel);
        addPrintLine("Email Register:", email, startY + 2 * rowH, docPanel);
        addPrintLine("Degree Major:", course, startY + 3 * rowH, docPanel);
        addPrintLine("Cumulative GPA:", String.format("%.2f / 4.00", gpa), startY + 4 * rowH, docPanel);
        addPrintLine("Standing Status:", standing, startY + 5 * rowH, docPanel);

        JPanel seal = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(230, 126, 34));
                g2.setStroke(new BasicStroke(2));
                g2.drawOval(2, 2, 45, 45);
                g2.setFont(new Font("Georgia", Font.BOLD, 10));
                g2.drawString("SEAL", 12, 27);
            }
        };
        seal.setBounds(50, 310, 50, 50);
        seal.setOpaque(false);
        docPanel.add(seal);

        JLabel sign = new JLabel("Registrar Office Signature");
        sign.setFont(new Font("Georgia", Font.ITALIC | Font.BOLD, 11));
        sign.setForeground(new Color(44, 62, 80));
        sign.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(44, 62, 80)));
        sign.setBounds(220, 335, 180, 20);
        sign.setHorizontalAlignment(SwingConstants.CENTER);
        docPanel.add(sign);

        dialog.add(docPanel, BorderLayout.CENTER);

        JButton printBtn = new JButton("Print Transcript");
        printBtn.setBackground(new Color(44, 62, 80));
        printBtn.setForeground(Color.WHITE);
        printBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        printBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(dialog, "Sending document to local system print queue...", "Printing", JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
        });
        dialog.add(printBtn, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void addPrintLine(String label, String value, int y, JPanel p) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(127, 140, 141));
        lbl.setBounds(40, y, 110, 20);
        p.add(lbl);

        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.BOLD, 13));
        val.setForeground(new Color(44, 62, 80));
        val.setBounds(155, y, 250, 20);
        p.add(val);
    }

    // Custom GPA Bar Chart Panel for Dashboard
    private class GpaChartPanel extends JPanel {
        private int[] counts = new int[4]; // [3.5-4.0, 3.0-3.5, 2.5-3.0, <2.5]

        public void setCounts(int[] counts) {
            this.counts = counts;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int margin = 35;
            int chartH = h - 2 * margin;
            int chartW = w - 2 * margin;

            // Baseline
            g2d.setColor(new Color(130, 140, 155, 80));
            g2d.setStroke(new BasicStroke(1.5f));
            g2d.drawLine(margin, h - margin, w - margin, h - margin);

            int max = 1;
            for (int c : counts) {
                if (c > max) max = c;
            }

            String[] categories = {"Dean's (3.5+)", "First Class (3.0-3.5)", "Average (2.5-3.0)", "Failing (<2.5)"};
            Color[] colors = {
                    new Color(46, 204, 113), // Green
                    new Color(52, 152, 219), // Blue
                    new Color(241, 196, 15), // Yellow
                    new Color(231, 76, 60)   // Red
            };

            int barCount = 4;
            int colWidth = chartW / barCount;
            int barWidth = colWidth - 50;

            for (int i = 0; i < barCount; i++) {
                int count = counts[i];
                int barHeight = (int) (((double) count / max) * (chartH - 40));
                
                int x = margin + i * colWidth + 25;
                int y = h - margin - barHeight;

                // Draw Bar
                g2d.setColor(colors[i]);
                g2d.fillRoundRect(x, y, barWidth, barHeight, 8, 8);

                // Draw value
                g2d.setColor(isDarkMode ? Color.WHITE : Color.BLACK);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
                g2d.drawString(String.valueOf(count), x + barWidth / 2 - 5, y - 8);

                // Draw label
                g2d.setColor(new Color(130, 140, 155));
                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                FontMetrics fm = g2d.getFontMetrics();
                int lblWidth = fm.stringWidth(categories[i]);
                g2d.drawString(categories[i], x + barWidth / 2 - lblWidth / 2, h - margin + 20);
            }
        }
    }

    public static void main(String[] args) {
        // Setup modern FlatLaf theme on start
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception e) {
            System.err.println("FlatLaf failed. Using default.");
        }
        
        SwingUtilities.invokeLater(() -> {
            LoginDialog login = new LoginDialog(null);
            login.setVisible(true);
            if (login.isLoginSuccessful()) {
                MainFrame frame = new MainFrame(login.getSelectedRole(), login.getUsername());
                frame.setVisible(true);
            } else {
                System.exit(0);
            }
        });
    }
}
