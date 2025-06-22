package ui;

import database.Database;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class RegisterUI extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;

    public RegisterUI() {
        setTitle("TravelHotel - Registrasi");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbcMain = new GridBagConstraints();
        gbcMain.fill = GridBagConstraints.BOTH;
        gbcMain.weightx = 1;
        gbcMain.weighty = 1;

        // Kiri: Branding Panel
        JPanel leftPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, new Color(100, 149, 237), getWidth(), getHeight(), new Color(52, 58, 64)));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        leftPanel.setLayout(new GridBagLayout());
        leftPanel.setPreferredSize(new Dimension(500, 700));
        JLabel iconLabel = new JLabel("🏨");
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 80));
        iconLabel.setForeground(Color.WHITE);
        JLabel textLabel = new JLabel("TravelHotel");
        textLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        textLabel.setForeground(Color.WHITE);
        JPanel branding = new JPanel(new GridBagLayout());
        branding.setOpaque(false);
        GridBagConstraints bgbc = new GridBagConstraints();
        bgbc.gridy = 0;
        branding.add(iconLabel, bgbc);
        bgbc.gridy = 2;
        branding.add(textLabel, bgbc);
        leftPanel.add(branding);

        // Kanan: Register Form Panel
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setPreferredSize(new Dimension(500, 700));
        GridBagConstraints rbc = new GridBagConstraints();
        rbc.insets = new Insets(14, 24, 14, 24);
        rbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel welcomeLabel = new JLabel("Buat Akun TravelHotel");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        welcomeLabel.setForeground(new Color(52, 58, 64));
        rbc.gridx = 0;
        rbc.gridy = 0;
        rbc.gridwidth = 2;
        rbc.anchor = GridBagConstraints.CENTER;
        rightPanel.add(welcomeLabel, rbc);

        // Username
        rbc.gridy++;
        rbc.gridwidth = 1;
        rbc.anchor = GridBagConstraints.LINE_END;
        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        rightPanel.add(userLabel, rbc);

        rbc.gridx = 1;
        rbc.anchor = GridBagConstraints.LINE_START;
        usernameField = new JTextField(18);
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        rightPanel.add(usernameField, rbc);

        // Password
        rbc.gridx = 0;
        rbc.gridy++;
        rbc.anchor = GridBagConstraints.LINE_END;
        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        rightPanel.add(passLabel, rbc);

        rbc.gridx = 1;
        rbc.anchor = GridBagConstraints.LINE_START;
        passwordField = new JPasswordField(18);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        rightPanel.add(passwordField, rbc);

        // Tombol Register
        rbc.gridx = 0;
        rbc.gridy++;
        rbc.gridwidth = 2;
        rbc.anchor = GridBagConstraints.CENTER;
        JButton registerButton = new JButton("Register");
        registerButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        registerButton.setBackground(new Color(40, 167, 69));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFocusPainted(false);
        registerButton.setBorder(BorderFactory.createEmptyBorder(8, 32, 8, 32));
        registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        rightPanel.add(registerButton, rbc);

        // Tombol kembali ke login
        rbc.gridy++;
        JButton loginButton = new JButton("Sudah punya akun? Login di sini");
        loginButton.setFocusPainted(false);
        loginButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        loginButton.setForeground(new Color(100, 149, 237));
        loginButton.setBackground(Color.WHITE);
        loginButton.setBorderPainted(false);
        loginButton.setContentAreaFilled(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        rightPanel.add(loginButton, rbc);

        // Gabungkan panel kiri dan kanan ke mainPanel
        gbcMain.gridx = 0;
        mainPanel.add(leftPanel, gbcMain);
        gbcMain.gridx = 1;
        mainPanel.add(rightPanel, gbcMain);

        setContentPane(mainPanel);

        // Action listeners
        registerButton.addActionListener(e -> handleRegister());
        loginButton.addActionListener(e -> {
            dispose();
            new LoginUI().setVisible(true);
        });
    }

    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username dan Password harus diisi!");
            return;
        }

        try (Connection conn = Database.connect()) {
            String check = "SELECT * FROM users WHERE username=?";
            PreparedStatement checkStmt = conn.prepareStatement(check);
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "Username sudah terdaftar!");
                return;
            }

            String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, 'user')";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Registrasi berhasil! Silakan login.");
            dispose();
            new LoginUI().setVisible(true);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal registrasi: " + e.getMessage());
        }
    }
}