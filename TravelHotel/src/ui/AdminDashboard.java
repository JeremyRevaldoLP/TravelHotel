package ui;

import database.Database;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class AdminDashboard extends JFrame {
    private JTable table;
    private DefaultTableModel model;

    public AdminDashboard() {
        setTitle("TravelHotel - Admin Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screenSize.width, screenSize.height);
        setLocation(0, 0);
        setLayout(new BorderLayout());

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
        sidebar.setPreferredSize(new Dimension(220, screenSize.height));
        sidebar.setOpaque(false);

        JLabel logoLabel = new JLabel("TravelHotel", SwingConstants.CENTER);
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        logoLabel.setForeground(Color.WHITE);
        logoLabel.setBorder(BorderFactory.createEmptyBorder(36, 10, 36, 10));
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(logoLabel);

        JButton btnAdd = createSidebarButton("Tambah Kamar");
        JButton btnEdit = createSidebarButton("Edit Kamar");
        JButton btnDelete = createSidebarButton("Hapus Kamar");
        JButton btnLogout = createSidebarButton("Logout");

        sidebar.add(Box.createVerticalStrut(20));
        sidebar.add(btnAdd);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(btnEdit);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(btnDelete);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(btnLogout);

        // TABEL KAMAR
        model = new DefaultTableModel(new String[]{"ID", "Nama", "Tipe", "Harga/Hari", "Tersedia", "Gambar"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.getTableHeader().setBackground(new Color(100, 149, 237));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(220, 230, 250));
        table.setSelectionForeground(Color.BLACK);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 16));
        // Set renderer kolom "Tersedia" agar wrap
        table.getColumnModel().getColumn(0).setPreferredWidth(40);   // ID
        table.getColumnModel().getColumn(1).setPreferredWidth(120);  // Nama
        table.getColumnModel().getColumn(2).setPreferredWidth(80);   // Tipe
        table.getColumnModel().getColumn(3).setPreferredWidth(90);   // Harga/Hari
        table.getColumnModel().getColumn(4).setPreferredWidth(180);  // Tersedia (wrap)
        table.getColumnModel().getColumn(4).setCellRenderer(new MultiLineCellRenderer());
        table.getColumnModel().getColumn(5).setPreferredWidth(120);  // Gambar
        loadKamar();

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 149, 237), 2),
                "Daftar Kamar",
                0, 0, new Font("Segoe UI", Font.BOLD, 20), new Color(100, 149, 237)
        ));

        // MAIN PANEL
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(32, 32, 32, 32));
        mainPanel.add(tableScroll, BorderLayout.CENTER);

        // Gabungkan sidebar dan mainPanel
        add(sidebar, BorderLayout.WEST);
        add(mainPanel, BorderLayout.CENTER);

        // ACTIONS
        btnAdd.addActionListener(e -> tambahKamar());
        btnEdit.addActionListener(e -> editKamar());
        btnDelete.addActionListener(e -> hapusKamar());
        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Apakah Anda yakin ingin logout?",
                    "Konfirmasi Logout",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                new LoginUI().setVisible(true);
            }
        });
    }

    private JButton createSidebarButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 15));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(180, 40));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBackground(new Color(36, 52, 92));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 10));
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(100, 149, 237));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(36, 52, 92));
            }
        });
        return button;
    }

    private void loadKamar() {
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

        model.setRowCount(0); // hapus data lama

        try (Connection conn = Database.connect()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM kamar");

            while (rs.next()) {
                int kamarId = rs.getInt("id");
                boolean tersedia = rs.getBoolean("tersedia");
                String statusTersedia = "Ya";

                if (!tersedia) {
                    // Ambil booking yang masih aktif (tanggal_checkout >= hari ini dan tidak NULL)
                    String sqlBooking = "SELECT u.username, b.tanggal_checkin, b.tanggal_checkout " +
                            "FROM booking b JOIN users u ON b.user_id = u.id " +
                            "WHERE b.kamar_id = ? AND b.tanggal_checkout IS NOT NULL AND b.tanggal_checkout >= CURDATE() " +
                            "ORDER BY b.tanggal_checkout ASC LIMIT 1";
                    try (PreparedStatement stmtB = conn.prepareStatement(sqlBooking)) {
                        stmtB.setInt(1, kamarId);
                        ResultSet rsB = stmtB.executeQuery();
                        if (rsB.next()) {
                            String username = rsB.getString("username");
                            Date tglCheckin = rsB.getDate("tanggal_checkin");
                            Date tglCheckout = rsB.getDate("tanggal_checkout");
                            statusTersedia = String.format("Tidak\n(%s,\n%s - %s)", username,
                                    tglCheckin != null ? tglCheckin.toString() : "-",
                                    tglCheckout != null ? tglCheckout.toString() : "-");
                        } else {
                            // Jika tidak ada booking aktif, ambil booking terakhir (meskipun tanggal_checkout NULL)
                            String sqlLast = "SELECT u.username, b.tanggal_checkin, b.tanggal_checkout " +
                                    "FROM booking b JOIN users u ON b.user_id = u.id " +
                                    "WHERE b.kamar_id = ? ORDER BY b.tanggal_checkout DESC LIMIT 1";
                            try (PreparedStatement stmtLast = conn.prepareStatement(sqlLast)) {
                                stmtLast.setInt(1, kamarId);
                                ResultSet rsLast = stmtLast.executeQuery();
                                if (rsLast.next()) {
                                    String username = rsLast.getString("username");
                                    Date tglCheckin = rsLast.getDate("tanggal_checkin");
                                    Date tglCheckout = rsLast.getDate("tanggal_checkout");
                                    statusTersedia = String.format("Tidak\n(%s,\n%s - %s)", username,
                                            tglCheckin != null ? tglCheckin.toString() : "-",
                                            tglCheckout != null ? tglCheckout.toString() : "-");
                                } else {
                                    statusTersedia = "Tidak";
                                }
                            }
                        }
                    }
                }

                model.addRow(new Object[]{
                        kamarId,
                        rs.getString("nama"),
                        rs.getString("tipe"),
                        rs.getDouble("harga"),
                        statusTersedia,
                        rs.getString("gambar")
                });
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data kamar: " + e.getMessage());
        }
    }

    private void tambahKamar() {
        JTextField nama = new JTextField();
        JTextField tipe = new JTextField();
        JTextField harga = new JTextField();
        JTextField gambar = new JTextField();
        JButton pilihGambarBtn = new JButton("Pilih Gambar");

        pilihGambarBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                gambar.setText(fc.getSelectedFile().getAbsolutePath());
            }
        });

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Nama:"), gbc);
        gbc.gridx = 1; panel.add(nama, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Tipe:"), gbc);
        gbc.gridx = 1; panel.add(tipe, gbc);
        gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("Harga/Hari:"), gbc);
        gbc.gridx = 1; panel.add(harga, gbc);
        gbc.gridx = 0; gbc.gridy = 3; panel.add(new JLabel("Gambar:"), gbc);
        gbc.gridx = 1; panel.add(gambar, gbc);
        gbc.gridx = 2; panel.add(pilihGambarBtn, gbc);

        int result = JOptionPane.showConfirmDialog(this, panel, "Tambah Kamar", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try (Connection conn = Database.connect()) {
                String sql = "INSERT INTO kamar (nama, tipe, harga, tersedia, gambar) VALUES (?, ?, ?, TRUE, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, nama.getText());
                stmt.setString(2, tipe.getText());
                stmt.setDouble(3, Double.parseDouble(harga.getText()));
                stmt.setString(4, gambar.getText());
                stmt.executeUpdate();
                loadKamar();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Gagal menambah kamar: " + e.getMessage());
            }
        }
    }

    private void editKamar() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Pilih kamar yang ingin diedit!");
            return;
        }

        int id = (int) model.getValueAt(row, 0);
        String namaAwal = (String) model.getValueAt(row, 1);
        String tipeAwal = (String) model.getValueAt(row, 2);
        double hargaAwal = (double) model.getValueAt(row, 3);
        String tersediaAwal = (String) model.getValueAt(row, 4);
        String gambarAwal = (String) model.getValueAt(row, 5);

        JTextField nama = new JTextField(namaAwal);
        JTextField tipe = new JTextField(tipeAwal);
        JTextField harga = new JTextField(String.valueOf(hargaAwal));
        JTextField gambar = new JTextField(gambarAwal);
        JCheckBox tersedia = new JCheckBox("Tersedia", tersediaAwal.startsWith("Ya"));
        JButton pilihGambarBtn = new JButton("Pilih Gambar");

        pilihGambarBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                gambar.setText(fc.getSelectedFile().getAbsolutePath());
            }
        });

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Nama:"), gbc);
        gbc.gridx = 1; panel.add(nama, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Tipe:"), gbc);
        gbc.gridx = 1; panel.add(tipe, gbc);
        gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("Harga/Hari:"), gbc);
        gbc.gridx = 1; panel.add(harga, gbc);
        gbc.gridx = 0; gbc.gridy = 3; panel.add(new JLabel("Gambar:"), gbc);
        gbc.gridx = 1; panel.add(gambar, gbc);
        gbc.gridx = 2; panel.add(pilihGambarBtn, gbc);
        gbc.gridx = 0; gbc.gridy = 4; panel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1; panel.add(tersedia, gbc);

        int result = JOptionPane.showConfirmDialog(this, panel, "Edit Kamar", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try (Connection conn = Database.connect()) {
                String sql = "UPDATE kamar SET nama=?, tipe=?, harga=?, gambar=?, tersedia=? WHERE id=?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, nama.getText());
                stmt.setString(2, tipe.getText());
                stmt.setDouble(3, Double.parseDouble(harga.getText()));
                stmt.setString(4, gambar.getText());
                stmt.setBoolean(5, tersedia.isSelected());
                stmt.setInt(6, id);
                stmt.executeUpdate();
                loadKamar();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Gagal mengedit kamar: " + e.getMessage());
            }
        }
    }

    private void hapusKamar() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Pilih kamar yang ingin dihapus!");
            return;
        }

        int id = (int) model.getValueAt(row, 0);

        int confirm = JOptionPane.showConfirmDialog(this, "Yakin ingin menghapus kamar ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = Database.connect()) {
                String sql = "DELETE FROM kamar WHERE id=?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, id);
                stmt.executeUpdate();
                loadKamar();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Gagal menghapus kamar: " + e.getMessage());
            }
        }
    }
}

// Renderer untuk wrap text pada kolom "Tersedia"
class MultiLineCellRenderer extends JTextArea implements TableCellRenderer {
    public MultiLineCellRenderer() {
        setLineWrap(true);
        setWrapStyleWord(true);
        setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        setText(value == null ? "" : value.toString());
        setFont(table.getFont());
        setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
        setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
        setBorder(null);

        // Penyesuaian tinggi baris otomatis
        int currentHeight = getPreferredSize().height;
        if (table.getRowHeight(row) != currentHeight) {
            table.setRowHeight(row, currentHeight);
        }
        return this;
    }
}