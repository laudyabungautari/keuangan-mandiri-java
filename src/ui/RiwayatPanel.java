package ui;

import storage.TransaksiService;
import util.MoneyUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDate;

public class RiwayatPanel extends JPanel {

    private final DashboardFrame frame;

    // THEME
    private final Color BG = new Color(245, 247, 250);
    private final Color CARD = Color.WHITE;
    private final Color TEXT = new Color(31, 41, 55);
    private final Color MUTED = new Color(107, 114, 128);
    private final Color PRIMARY = new Color(99, 102, 241);
    private final Color DANGER = new Color(239, 68, 68);

    private final JTextField tfTanggal = new JTextField(10); // YYYY-MM-DD optional
    private final JTextField tfCari = new JTextField(18);
    private final JLabel lblInfo = new JLabel(" ");

    private final DefaultTableModel tableModel;
    private final JTable table;
    private final TableRowSorter<DefaultTableModel> sorter;

    public RiwayatPanel(DashboardFrame frame) {
        this.frame = frame;

        setLayout(new BorderLayout(12, 12));
        setBackground(BG);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        // ===== HEADER =====
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(BG);

        JLabel title = new JLabel("Riwayat Transaksi");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(TEXT);

        JLabel sub = new JLabel("Klik 1x baris lalu Edit/Hapus, atau double click untuk edit.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(MUTED);

        header.add(title);
        header.add(Box.createVerticalStrut(6));
        header.add(sub);

        add(header, BorderLayout.NORTH);

        // ===== CARD =====
        JPanel card = new JPanel(new BorderLayout(12, 12));
        card.setBackground(CARD);
        card.setBorder(new EmptyBorder(16, 16, 16, 16));
        add(card, BorderLayout.CENTER);

        // ===== TOOLBAR =====
        JPanel toolbar = new JPanel(new GridBagLayout());
        toolbar.setBackground(CARD);

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblTgl = new JLabel("Tanggal:");
        lblTgl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblTgl.setForeground(MUTED);

        JLabel lblCari = new JLabel("Cari keterangan:");
        lblCari.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblCari.setForeground(MUTED);

        JButton btnTampilkan = new JButton("Tampilkan");
        JButton btnReset = new JButton("Reset");
        JButton btnEdit = new JButton("Edit");
        JButton btnHapus = new JButton("Hapus");

        // style tombol (FlatLaf friendly)
        stylePrimary(btnEdit);
        styleDanger(btnHapus);
        styleSoft(btnTampilkan);
        styleSoft(btnReset);

        g.gridx = 0; g.gridy = 0; g.weightx = 0;
        toolbar.add(lblTgl, g);

        g.gridx = 1; g.weightx = 1;
        tfTanggal.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        toolbar.add(tfTanggal, g);

        g.gridx = 2; g.weightx = 0;
        toolbar.add(lblCari, g);

        g.gridx = 3; g.weightx = 1;
        tfCari.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        toolbar.add(tfCari, g);

        g.gridx = 4; g.weightx = 0;
        toolbar.add(btnTampilkan, g);

        g.gridx = 5;
        toolbar.add(btnReset, g);

        g.gridx = 6;
        toolbar.add(btnEdit, g);

        g.gridx = 7;
        toolbar.add(btnHapus, g);

        card.add(toolbar, BorderLayout.NORTH);

        // ===== TABLE =====
        String[] cols = {"ID", "Tanggal", "Tipe", "Jumlah", "Keterangan"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(32);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // clean look
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));

        // zebra rows
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                }
                return c;
            }
        });

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        card.add(scroll, BorderLayout.CENTER);

        // ===== INFO =====
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblInfo.setForeground(MUTED);
        card.add(lblInfo, BorderLayout.SOUTH);

        // ===== EVENTS =====
        btnTampilkan.addActionListener(e -> loadData());
        btnReset.addActionListener(e -> {
            tfTanggal.setText("");
            tfCari.setText("");
            loadData();
        });

        btnEdit.addActionListener(e -> editSelectedRow());
        btnHapus.addActionListener(e -> hapusSelectedRow());

        // double click edit
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    editSelectedRow();
                }
            }
        });

        // live search keterangan
        tfCari.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applySearchFilter(); }
            @Override public void removeUpdate(DocumentEvent e) { applySearchFilter(); }
            @Override public void changedUpdate(DocumentEvent e) { applySearchFilter(); }
        });

        loadData();
    }

    private void stylePrimary(JButton b) {
        b.putClientProperty("JButton.buttonType", "roundRect");
        b.setBackground(PRIMARY);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
    }

    private void styleDanger(JButton b) {
        b.putClientProperty("JButton.buttonType", "roundRect");
        b.setBackground(DANGER);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
    }

    private void styleSoft(JButton b) {
        b.putClientProperty("JButton.buttonType", "roundRect");
        b.setFocusPainted(false);
    }

    private void applySearchFilter() {
        String keyword = tfCari.getText().trim();
        if (keyword.isEmpty()) {
            sorter.setRowFilter(null);
            return;
        }
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(keyword), 4));
    }

    public final void loadData() {
        try {
            tableModel.setRowCount(0);

            LocalDate filterTgl = null;
            String tanggalStr = tfTanggal.getText().trim();
            if (!tanggalStr.isEmpty()) {
                try {
                    filterTgl = LocalDate.parse(tanggalStr);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Format tanggal salah. Contoh: 2025-12-22");
                    tfTanggal.requestFocus();
                    return;
                }
            }

            java.util.List<model.Transaksi> list = TransaksiService.getAll();

            int count = 0;
            long sumIn = 0, sumOut = 0;

            for (model.Transaksi t : list) {
                if (filterTgl != null && !t.getTanggal().equals(filterTgl)) continue;

                tableModel.addRow(new Object[]{
                        t.getId(),
                        t.getTanggal().toString(),
                        t.getTipe(),
                        MoneyUtil.rupiah(t.getJumlah()),
                        t.getKeterangan() == null ? "" : t.getKeterangan()
                });
                count++;

                if (t.isPemasukan()) sumIn += t.getJumlah();
                if (t.isPengeluaran()) sumOut += t.getJumlah();
            }

            lblInfo.setText("Total tampil: " + count +
                    " | Pemasukan: " + MoneyUtil.rupiah(sumIn) +
                    " | Pengeluaran: " + MoneyUtil.rupiah(sumOut) +
                    " | Balance: " + MoneyUtil.rupiah(sumIn - sumOut));

            applySearchFilter();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal load riwayat: " + e.getMessage());
        }
    }

    private void hapusSelectedRow() {
        try {
            int viewRow = table.getSelectedRow();
            if (viewRow < 0) {
                JOptionPane.showMessageDialog(this, "Pilih dulu transaksi yang mau dihapus.");
                return;
            }

            int modelRow = table.convertRowIndexToModel(viewRow);
            int id = Integer.parseInt(tableModel.getValueAt(modelRow, 0).toString());

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Yakin mau hapus transaksi ID " + id + "?",
                    "Konfirmasi Hapus",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm != JOptionPane.YES_OPTION) return;

            boolean ok = TransaksiService.hapusById(id);
            if (ok) {
                JOptionPane.showMessageDialog(this, "✅ Berhasil hapus ID " + id);
                loadData();
                if (frame != null) frame.refreshDashboard();
            } else {
                JOptionPane.showMessageDialog(this, "❌ ID tidak ditemukan / gagal hapus.");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "❌ Gagal hapus: " + e.getMessage());
        }
    }

    // ===== EDIT (MODERN DIALOG) =====
    private void editSelectedRow() {
        try {
            int viewRow = table.getSelectedRow();
            if (viewRow < 0) {
                JOptionPane.showMessageDialog(this, "Pilih dulu transaksi yang mau diedit.");
                return;
            }

            int modelRow = table.convertRowIndexToModel(viewRow);

            int id = Integer.parseInt(tableModel.getValueAt(modelRow, 0).toString());
            String tanggalOld = tableModel.getValueAt(modelRow, 1).toString();
            String tipeOld = tableModel.getValueAt(modelRow, 2).toString();
            String jumlahOld = tableModel.getValueAt(modelRow, 3).toString();
            String ketOld = tableModel.getValueAt(modelRow, 4).toString();

            EditTransaksiDialog dlg = new EditTransaksiDialog(
                    SwingUtilities.getWindowAncestor(this),
                    id, tanggalOld, tipeOld, jumlahOld, ketOld
            );
            dlg.setVisible(true);

            if (!dlg.isSaved()) return;

            boolean updated = TransaksiService.updateById(
                    id,
                    dlg.getTanggal(),
                    dlg.getTipe(),
                    dlg.getJumlah(),
                    dlg.getKeterangan()
            );

            if (updated) {
                JOptionPane.showMessageDialog(this, "✅ Berhasil update ID " + id);
                loadData();
                if (frame != null) frame.refreshDashboard();
            } else {
                JOptionPane.showMessageDialog(this, "❌ Gagal update. ID tidak ditemukan.");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "❌ Gagal edit: " + e.getMessage());
        }
    }
}
