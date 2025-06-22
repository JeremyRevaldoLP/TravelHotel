package ui;

import database.Database;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LoginUI extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginUI() {
        setTitle("TravelHotel - Login");
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screenSize.width, screenSize.height);
        setLocation(0, 0);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(245, 245, 250));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;

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
        leftPanel.setPreferredSize(new Dimension(500, 600));
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
        bgbc.gridy = 1;
        branding.add(Box.createVerticalStrut(20), bgbc);
        bgbc.gridy = 2;
        branding.add(textLabel, bgbc);
        leftPanel.add(branding);

        // Kanan: Login Form Panel
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setPreferredSize(new Dimension(500, 600));
        GridBagConstraints rbc = new GridBagConstraints();
        rbc.insets = new Insets(16, 24, 16, 24);
        rbc.fill = GridBagConstraints.HORIZONTAL;

        // Welcome
        JLabel welcomeLabel = new JLabel("Selamat Datang Kembali!");
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

        // Tombol Login & Register
        rbc.gridx = 0;
        rbc.gridy++;
        rbc.gridwidth = 2;
        rbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 0));
        buttonPanel.setOpaque(false);
        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");
        Font btnFont = new Font("Segoe UI", Font.BOLD, 16);
        Color btnColor = new Color(100, 149, 237);
        Color btnText = Color.WHITE;
        loginButton.setFont(btnFont);
        loginButton.setBackground(btnColor);
        loginButton.setForeground(btnText);
        loginButton.setFocusPainted(false);
        loginButton.setBorder(BorderFactory.createEmptyBorder(8, 32, 8, 32));
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerButton.setFont(btnFont);
        registerButton.setBackground(new Color(40, 167, 69));
        registerButton.setForeground(btnText);
        registerButton.setFocusPainted(false);
        registerButton.setBorder(BorderFactory.createEmptyBorder(8, 32, 8, 32));
        registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        rightPanel.add(buttonPanel, rbc);

        // Gabungkan panel kiri dan kanan ke mainPanel
        gbc.gridx = 0;
        mainPanel.add(leftPanel, gbc);
        gbc.gridx = 1;
        mainPanel.add(rightPanel, gbc);

        setContentPane(mainPanel);

        // Action listeners
        loginButton.addActionListener(e -> handleLogin());
        registerButton.addActionListener(e -> new RegisterUI().setVisible(true));
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        try (Connection conn = Database.connect()) {
            String query = "SELECT * FROM users WHERE username=? AND password=?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                JOptionPane.showMessageDialog(this, "Login berhasil sebagai " + role + "!");
                dispose();

                if (role.equals("admin")) {
                    new AdminDashboard().setVisible(true);
                } else {
                    new UserDashboard(rs.getInt("id")).setVisible(true);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Username atau password salah.", "Login Gagal", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Koneksi gagal: " + e.getMessage());
        }
    }
}