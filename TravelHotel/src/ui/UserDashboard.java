package ui;

import database.Database;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

import com.toedter.calendar.JDateChooser;

public class UserDashboard extends JFrame {
    private JTable tableKamar;
    private DefaultTableModel kamarModel;
    private int userId;

    // Komponen filter
    private JComboBox<String> tipeCombo;
    private JTextField minHargaField;
    private JTextField maxHargaField;

    public UserDashboard(int userId) {
        this.userId = userId;
        setTitle("TravelHotel - Dashboard Pengguna");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Ukuran window
        setSize(1200, 700);
        setLocationRelativeTo(null);

        // Main panel (GridBagLayout agar sidebar & konten proporsional)
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(245, 245, 250));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;

        // SIDEBAR
        JPanel sidebar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setPaint(new GradientPaint(
                        0, 0, new Color(100, 149, 237),
                        0, getHeight(), new Color(52, 58, 64)));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(220, 700));
        sidebar.setOpaque(false);

        JLabel logoLabel = new JLabel("TravelHotel", SwingConstants.CENTER);
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        logoLabel.setForeground(Color.WHITE);
        logoLabel.setBorder(BorderFactory.createEmptyBorder(36, 10, 36, 10));
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(logoLabel);

        JButton riwayatBtn = new JButton("Riwayat Booking");
        JButton logoutBtn = new JButton("Logout");
        styleSidebarButton(riwayatBtn);
        styleSidebarButton(logoutBtn);

        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(riwayatBtn);
        sidebar.add(Box.createVerticalStrut(10));

        // === FILTER PANEL DI SIDEBAR ===
        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.Y_AXIS));
        filterPanel.setBackground(new Color(100, 149, 237));
        filterPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.WHITE, 1),
            "Filter Kamar",
            0, 0, new Font("Segoe UI", Font.BOLD, 14), Color.WHITE
        ));

        JLabel tipeLabel = new JLabel("Tipe:");
        tipeLabel.setForeground(Color.WHITE);
        tipeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        filterPanel.add(tipeLabel);

        String[] tipeOptions = {"Semua", "Standar", "Deluxe", "Suite", "VIP"};
        tipeCombo = new JComboBox<>(tipeOptions);
        tipeCombo.setMaximumSize(new Dimension(180, 25));
        filterPanel.add(tipeCombo);

        JLabel hargaLabel = new JLabel("Harga:");
        hargaLabel.setForeground(Color.WHITE);
        hargaLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        filterPanel.add(hargaLabel);

        JPanel hargaPanel = new JPanel();
        hargaPanel.setOpaque(false);
        hargaPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 2));
        minHargaField = new JTextField(5);
        maxHargaField = new JTextField(5);
        hargaPanel.add(new JLabel("Min"));
        hargaPanel.add(minHargaField);
        hargaPanel.add(new JLabel("Max"));
        hargaPanel.add(maxHargaField);
        filterPanel.add(hargaPanel);

        JButton filterBtn = new JButton("Cari");
        filterBtn.setBackground(Color.WHITE);
        filterBtn.setForeground(new Color(100, 149, 237));
        filterBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        filterBtn.setFocusPainted(false);
        filterBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        filterPanel.add(Box.createVerticalStrut(5));
        filterPanel.add(filterBtn);

        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(filterPanel);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(logoutBtn);
        sidebar.add(Box.createVerticalStrut(18));

        // Action Sidebar
        riwayatBtn.addActionListener(e -> showRiwayatPanel());
        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Apakah Anda yakin ingin logout?",
                    "Konfirmasi Logout",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                new LoginUI().setVisible(true);
            }
        });

        // PANEL KONTEN UTAMA
        JPanel contentPanel = new JPanel(new BorderLayout(20, 20));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(32, 32, 32, 32));

        // Ambil username dari database
        String username = "";
        try (Connection conn = Database.connect()) {
            String sql = "SELECT username FROM users WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                username = rs.getString("username");
            }
        } catch (SQLException e) {
            username = "";
        }

        // Judul
        JLabel title = new JLabel("Selamat Datang di TravelHotel" + (username.isEmpty() ? "" : ", " + username), SwingConstants.LEFT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(new Color(52, 58, 64));
        contentPanel.add(title, BorderLayout.BEFORE_FIRST_LINE);

        // Table styling
        kamarModel = new DefaultTableModel(new String[]{"Nama", "Tipe", "Harga/Hari"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Semua sel tidak bisa diedit
            }
        };

        tableKamar = new JTable(kamarModel);
        tableKamar.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        tableKamar.setRowHeight(26);
        tableKamar.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 18));
        tableKamar.setSelectionBackground(new Color(220, 230, 250));
        tableKamar.setSelectionForeground(Color.BLACK);
        tableKamar.setShowGrid(false);
        tableKamar.setIntercellSpacing(new Dimension(0, 0));
        loadKamarTersedia("Semua", "", "");

        JScrollPane scrollPane = new JScrollPane(tableKamar);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 149, 237), 2),
                "Daftar Kamar Tersedia",
                0, 0, new Font("Segoe UI", Font.BOLD, 20), new Color(100, 149, 237)
        ));
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        // Panel slider gambar
        JPanel sliderPanel = new JPanel(new BorderLayout());
        sliderPanel.setBackground(Color.WHITE);
        sliderPanel.setPreferredSize(new Dimension(300, 220));
        JLabel imageLabel = new JLabel("Pilih kamar untuk melihat gambar", SwingConstants.CENTER);
        imageLabel.setFont(new Font("Segoe UI", Font.ITALIC, 16));
        sliderPanel.add(imageLabel, BorderLayout.CENTER);
        contentPanel.add(sliderPanel, BorderLayout.EAST);

        // MouseListener untuk klik baris tabel
        tableKamar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = tableKamar.getSelectedRow();
                if (row != -1) {
                    String namaKamar = (String) kamarModel.getValueAt(row, 0);
                    tampilkanGambarKamar(namaKamar, imageLabel);
                }
            }
        });

        // Panel tombol bawah
        JPanel panelBtn = new JPanel(new FlowLayout(FlowLayout.CENTER, 32, 10));
        panelBtn.setBackground(Color.WHITE);
        JButton btnBooking = new JButton("Booking Kamar");
        JButton btnRefresh = new JButton("Refresh");
        for (JButton btn : new JButton[]{btnBooking, btnRefresh}) {
            btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
            btn.setBackground(new Color(100, 149, 237));
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createEmptyBorder(8, 32, 8, 32));
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        panelBtn.add(btnBooking);
        panelBtn.add(btnRefresh);
        contentPanel.add(panelBtn, BorderLayout.SOUTH);

        // Gabungkan sidebar dan konten ke mainPanel
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.gridheight = 2;
        mainPanel.add(sidebar, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridheight = 2;
        mainPanel.add(contentPanel, gbc);

        setContentPane(mainPanel);

        // Action listeners
        btnBooking.addActionListener(e -> bookingKamar());
        btnRefresh.addActionListener(e -> {
            tipeCombo.setSelectedIndex(0);
            minHargaField.setText("");
            maxHargaField.setText("");
            loadKamarTersedia("Semua", "", "");
        });
        filterBtn.addActionListener(e -> {
            String tipe = (String) tipeCombo.getSelectedItem();
            String minHarga = minHargaField.getText().trim();
            String maxHarga = maxHargaField.getText().trim();
            loadKamarTersedia(tipe, minHarga, maxHarga);
        });
        // Default tampil kamar
        showKamarPanel();
    }

    private void styleSidebarButton(JButton button) {
        Color normalColor = new Color(36, 52, 92);
        Color hoverColor = new Color(100, 149, 237);
        Color textColor = Color.WHITE;

        button.setFocusPainted(false);
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setBackground(normalColor);
        button.setForeground(textColor);
        button.setFont(new Font("Segoe UI", Font.BOLD, 15));
        button.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 10));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(180, 40));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(hoverColor);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(normalColor);
            }
        });
    }

    private void showKamarPanel() {
        loadKamarTersedia("Semua", "", "");
    }

    private void showRiwayatPanel() {
        DefaultTableModel riwayatModel = new DefaultTableModel(
            new String[]{"Kamar", "Check-in", "Check-out"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        try (Connection conn = Database.connect()) {
            String sql = """
                    SELECT b.id, k.nama, b.tanggal_checkin, b.tanggal_checkout
                    FROM booking b
                    JOIN kamar k ON b.kamar_id = k.id
                    WHERE b.user_id = ?
                    ORDER BY b.tanggal_checkin DESC
                    """;
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                riwayatModel.addRow(new Object[]{
                    rs.getInt("id"), // simpan id booking (hidden)
                    rs.getString("nama"),
                    rs.getDate("tanggal_checkin"),
                    rs.getDate("tanggal_checkout")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat riwayat: " + e.getMessage());
            return;
        }

        JTable riwayatTable = new JTable(riwayatModel) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        riwayatTable.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        riwayatTable.setRowHeight(24);
        riwayatTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 16));
        riwayatTable.setBackground(Color.WHITE);

        // Sembunyikan kolom id booking
        riwayatTable.removeColumn(riwayatTable.getColumnModel().getColumn(0));

        JScrollPane scrollPane = new JScrollPane(riwayatTable);
        scrollPane.setPreferredSize(new Dimension(480, 220));
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(100, 149, 237), 2),
            "Riwayat Booking Anda",
            0, 0, new Font("Segoe UI", Font.BOLD, 18), new Color(100, 149, 237)
        ));

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Tambahkan mouse listener untuk receipt
        riwayatTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = riwayatTable.getSelectedRow();
                if (row != -1) {
                    // Ambil id booking dari model (meski kolom disembunyikan)
                    int bookingId = (int) riwayatModel.getValueAt(row, 0);
                    showReceiptDialog(bookingId);
                }
            }
        });

        JOptionPane.showMessageDialog(this, panel, "Riwayat Booking", JOptionPane.PLAIN_MESSAGE);
    }

    // Tambahkan method baru untuk menampilkan receipt
    private void showReceiptDialog(int bookingId) {
        try (Connection conn = Database.connect()) {
            String sql = """
                SELECT b.id, u.username, k.nama AS kamar, k.tipe, k.harga, b.tanggal_checkin, b.tanggal_checkout
                FROM booking b
                JOIN users u ON b.user_id = u.id
                JOIN kamar k ON b.kamar_id = k.id
                WHERE b.id = ?
            """;
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, bookingId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                StringBuilder sb = new StringBuilder();
                sb.append("=== RECEIPT BOOKING HOTEL ===\n\n");
                sb.append("Booking ID   : ").append(rs.getInt("id")).append("\n");
                sb.append("Nama User    : ").append(rs.getString("username")).append("\n");
                sb.append("Nama Kamar   : ").append(rs.getString("kamar")).append("\n");
                sb.append("Tipe Kamar   : ").append(rs.getString("tipe")).append("\n");
                sb.append("Harga/Hari   : ").append(formatRupiah(rs.getDouble("harga"))).append("\n");
                sb.append("Check-in     : ").append(rs.getDate("tanggal_checkin")).append("\n");
                sb.append("Check-out    : ").append(rs.getDate("tanggal_checkout")).append("\n");
                sb.append("\nTunjukkan receipt ini ke administrasi hotel saat check-in.\n");

                JTextArea area = new JTextArea(sb.toString());
                area.setEditable(false);
                area.setFont(new Font("Monospaced", Font.PLAIN, 14));
                area.setBackground(Color.WHITE);
                JOptionPane.showMessageDialog(this, new JScrollPane(area), "Receipt Booking", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat receipt: " + e.getMessage());
        }
    }

    // Filter kamar tersedia
    private void loadKamarTersedia(String tipe, String minHarga, String maxHarga) {
        // Otomatis update status tersedia jika sudah lewat tanggal checkout terakhir
        try (Connection conn = Database.connect()) {
            String sql = "SELECT id FROM kamar WHERE tersedia=FALSE";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                int kamarId = rs.getInt("id");
                String sql2 = "SELECT MAX(tanggal_checkout) as terakhir FROM booking WHERE kamar_id=?";
                PreparedStatement stmt2 = conn.prepareStatement(sql2);
                stmt2.setInt(1, kamarId);
                ResultSet rs2 = stmt2.executeQuery();
                if (rs2.next()) {
                    Date terakhir = rs2.getDate("terakhir");
                    if (terakhir != null && terakhir.before(new java.sql.Date(System.currentTimeMillis()))) {
                        String sql3 = "UPDATE kamar SET tersedia=TRUE WHERE id=?";
                        PreparedStatement stmt3 = conn.prepareStatement(sql3);
                        stmt3.setInt(1, kamarId);
                        stmt3.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            // Optional: tampilkan pesan error jika perlu
        }

        kamarModel.setRowCount(0);
        StringBuilder sql = new StringBuilder("SELECT * FROM kamar WHERE tersedia=TRUE");
        if (!"Semua".equals(tipe)) {
            sql.append(" AND tipe=?");
        }
        if (!minHarga.isEmpty()) {
            sql.append(" AND harga>=?");
        }
        if (!maxHarga.isEmpty()) {
            sql.append(" AND harga<=?");
        }
        try (Connection conn = Database.connect()) {
            PreparedStatement stmt = conn.prepareStatement(sql.toString());
            int idx = 1;
            if (!"Semua".equals(tipe)) {
                stmt.setString(idx++, tipe);
            }
            if (!minHarga.isEmpty()) {
                stmt.setDouble(idx++, Double.parseDouble(minHarga));
            }
            if (!maxHarga.isEmpty()) {
                stmt.setDouble(idx++, Double.parseDouble(maxHarga));
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                kamarModel.addRow(new Object[]{
                    rs.getString("nama"),
                    rs.getString("tipe"),
                    formatRupiah(rs.getDouble("harga"))
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat kamar: " + e.getMessage());
        }
    }

    private String formatRupiah(double harga) {
        java.text.NumberFormat nf = java.text.NumberFormat.getInstance(new java.util.Locale("id", "ID"));
        return "Rp " + nf.format(harga);
    }

    private void bookingKamar() {
        int selected = tableKamar.getSelectedRow();
        if (selected == -1) {
            JOptionPane.showMessageDialog(this, "Pilih kamar terlebih dahulu!");
            return;
        }

        String namaKamar = (String) kamarModel.getValueAt(selected, 0);
        int kamarId = -1;

        // Cari ID kamar berdasarkan nama kamar
        try (Connection conn = Database.connect()) {
            String sql = "SELECT id FROM kamar WHERE nama = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, namaKamar);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                kamarId = rs.getInt("id");
            } else {
                JOptionPane.showMessageDialog(this, "Kamar tidak ditemukan di database!");
                return;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal mengambil ID kamar: " + e.getMessage());
            return;
        }

        JDateChooser checkinChooser = new JDateChooser();
        checkinChooser.setDateFormatString("yyyy-MM-dd");
        JDateChooser checkoutChooser = new JDateChooser();
        checkoutChooser.setDateFormatString("yyyy-MM-dd");

        JButton todayBtn = new JButton("Hari Ini");
        todayBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        todayBtn.setBackground(new Color(100, 149, 237));
        todayBtn.setForeground(Color.WHITE);
        todayBtn.setFocusPainted(false);
        todayBtn.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        todayBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        todayBtn.addActionListener(e -> checkinChooser.setDate(new java.util.Date()));

        JPanel bookingPanel = new JPanel(new GridBagLayout());
        bookingPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.LINE_START;

        // Label & field check-in
        gbc.gridx = 0; gbc.gridy = 0;
        bookingPanel.add(new JLabel("Tanggal Check-in:"), gbc);
        gbc.gridx = 1;
        bookingPanel.add(checkinChooser, gbc);
        gbc.gridx = 2;
        bookingPanel.add(todayBtn, gbc);

        // Label & field check-out
        gbc.gridx = 0; gbc.gridy = 1;
        bookingPanel.add(new JLabel("Tanggal Check-out:"), gbc);
        gbc.gridx = 1;
        bookingPanel.add(checkoutChooser, gbc);

        // Show dialog
        int result = JOptionPane.showConfirmDialog(this, bookingPanel, "Booking Kamar", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try (Connection conn = Database.connect()) {
                java.util.Date checkinDate = checkinChooser.getDate();
                java.util.Date checkoutDate = checkoutChooser.getDate();
                if (checkinDate == null || checkoutDate == null) {
                    JOptionPane.showMessageDialog(this, "Tanggal belum dipilih!");
                    return;
                }
                String sql = "INSERT INTO booking (user_id, kamar_id, tanggal_checkin, tanggal_checkout) VALUES (?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, userId);
                stmt.setInt(2, kamarId);
                stmt.setDate(3, new java.sql.Date(checkinDate.getTime()));
                stmt.setDate(4, new java.sql.Date(checkoutDate.getTime()));
                stmt.executeUpdate();

                // Set kamar tidak tersedia
                PreparedStatement stmt2 = conn.prepareStatement("UPDATE kamar SET tersedia=FALSE WHERE id=?");
                stmt2.setInt(1, kamarId);
                stmt2.executeUpdate();

                // Refresh dengan filter terakhir
                String tipe = (String) tipeCombo.getSelectedItem();
                String minHarga = minHargaField.getText().trim();
                String maxHarga = maxHargaField.getText().trim();
                loadKamarTersedia(tipe, minHarga, maxHarga);

                JOptionPane.showMessageDialog(this, "Booking berhasil!");

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Gagal booking: " + e.getMessage());
            }
        }
    }

    // Fungsi untuk menampilkan gambar kamar di panel slider
    private void tampilkanGambarKamar(String namaKamar, JLabel imageLabel) {
        String path = null;
        try (Connection conn = Database.connect()) {
            String sql = "SELECT gambar FROM kamar WHERE nama = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, namaKamar);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                path = rs.getString("gambar");
            }
        } catch (SQLException e) {
            imageLabel.setText("Gagal memuat gambar");
            imageLabel.setIcon(null);
            return;
        }

        if (path != null && !path.isEmpty()) {
            ImageIcon icon = new ImageIcon(path);
            Image img = icon.getImage().getScaledInstance(300, 220, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(img));
            imageLabel.setText("");
        } else {
            imageLabel.setIcon(null);
            imageLabel.setText("Gambar tidak tersedia");
        }
    }
}    