package ui;

import model.Transaksi;
import storage.TransaksiService;
import util.MoneyUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class DashboardFrame extends JFrame {

    // ===== THEME =====
    private final Color BG = new Color(245, 247, 251);
    private final Color TEXT = new Color(17, 24, 39);
    private final Color MUTED = new Color(107, 114, 128);

    // Card colors (mirip contoh)
    private final Color CARD_IN = new Color(46, 204, 113);   // hijau
    private final Color CARD_OUT = new Color(255, 99, 132);  // merah muda
    private final Color CARD_BAL = new Color(52, 172, 224);  // biru

    // ===== TOP CARDS =====
    private SummaryCard cardIn;
    private SummaryCard cardOut;
    private SummaryCard cardBal;

    // ===== TABLE =====
    private DefaultTableModel tableModel;
    private JTable table;

    // ===== PANELS (buat refresh semua) =====
    private RiwayatPanel riwayatPanel;

    // Filter mode (biar fleksibel)
    private String mode = "SEMUA"; // SEMUA | HARI | BULAN | TAHUN

    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy", new Locale("id", "ID"));

    public DashboardFrame() {
        setTitle("Keuangan Mandiri");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1050, 680);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 13));

        tabs.addTab("Dashboard", buildDashboardTab());
        tabs.addTab("Tambah Transaksi", new InputTransaksiPanel(this));

        riwayatPanel = new RiwayatPanel(this);
        tabs.addTab("Riwayat", riwayatPanel);

        tabs.addTab("Export", new ExportPanel());

        setContentPane(tabs);

        refreshDashboard();
    }

    // ======================================================
    // ================= DASHBOARD TAB ======================
    // ======================================================
    private JPanel buildDashboardTab() {
        JPanel root = new JPanel(new BorderLayout(16, 16));
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(18, 20, 18, 20));

        // ===== Header =====
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(TEXT);

        // Filter (optional tapi berguna)
        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        headerRight.setOpaque(false);

        JComboBox<String> cbMode = new JComboBox<>(new String[]{"Semua", "Hari Ini", "Bulan Ini", "Tahun Ini"});
        cbMode.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbMode.addActionListener(e -> {
            int idx = cbMode.getSelectedIndex();
            mode = (idx == 0) ? "SEMUA" : (idx == 1) ? "HARI" : (idx == 2) ? "BULAN" : "TAHUN";
            refreshDashboard();
        });

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.setFocusPainted(false);
        btnRefresh.addActionListener(e -> refreshAll());

        headerRight.add(cbMode);
        headerRight.add(btnRefresh);

        header.add(title, BorderLayout.WEST);
        header.add(headerRight, BorderLayout.EAST);

        root.add(header, BorderLayout.NORTH);

        // ===== Content =====
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        // --- Cards row ---
        JPanel cardsRow = new JPanel(new GridLayout(1, 3, 14, 0));
        cardsRow.setOpaque(false);

        cardIn = new SummaryCard("Total Pemasukan", "Rp 0", CARD_IN);
        cardOut = new SummaryCard("Total Pengeluaran", "Rp 0", CARD_OUT);
        cardBal = new SummaryCard("Saldo Akhir", "Rp 0", CARD_BAL);

        cardsRow.add(cardIn);
        cardsRow.add(cardOut);
        cardsRow.add(cardBal);

        content.add(Box.createVerticalStrut(6));
        content.add(cardsRow);
        content.add(Box.createVerticalStrut(16));

        // --- Table card ---
        JPanel tableCard = new JPanel(new BorderLayout(10, 10));
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235)),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel tableTitle = new JLabel("5 Transaksi Terakhir");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tableTitle.setForeground(MUTED);

        tableModel = new DefaultTableModel(new String[]{"Tanggal", "Kategori", "Keterangan", "Jumlah"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));

        // renderer jumlah + warna (hijau untuk pemasukan, merah untuk pengeluaran)
        table.getColumnModel().getColumn(3).setCellRenderer(new AmountRenderer());

        // renderer kategori jadi “badge feel” (tetap tanpa library)
        table.getColumnModel().getColumn(1).setCellRenderer(new BadgeRenderer());

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());

        tableCard.add(tableTitle, BorderLayout.NORTH);
        tableCard.add(sp, BorderLayout.CENTER);

        content.add(tableCard);

        root.add(content, BorderLayout.CENTER);

        return root;
    }

    // ======================================================
    // ===================== LOGIC ==========================
    // ======================================================
    public void refreshDashboard() {
        try {
            List<Transaksi> all = TransaksiService.getAll();

            // filter by mode
            LocalDate now = LocalDate.now();
            List<Transaksi> filtered = all.stream().filter(t -> {
                LocalDate d = t.getTanggal();
                if ("HARI".equals(mode)) return d.equals(now);
                if ("BULAN".equals(mode)) return d.getYear() == now.getYear() && d.getMonthValue() == now.getMonthValue();
                if ("TAHUN".equals(mode)) return d.getYear() == now.getYear();
                return true; // SEMUA
            }).collect(Collectors.toList());

            long in = 0;
            long out = 0;
            for (Transaksi t : filtered) {
                if (t.isPemasukan()) in += t.getJumlah();
                if (t.isPengeluaran()) out += t.getJumlah();
            }
            long bal = in - out;

            cardIn.setValue(MoneyUtil.rupiah(in));
            cardOut.setValue(MoneyUtil.rupiah(out));
            cardBal.setValue(MoneyUtil.rupiah(bal));

            // last 5 transactions (sort terbaru)
            List<Transaksi> last5 = filtered.stream()
                    .sorted(Comparator
                            .comparing(Transaksi::getTanggal).reversed()
                            .thenComparing(Transaksi::getId, Comparator.reverseOrder()))
                    .limit(5)
                    .collect(Collectors.toList());

            tableModel.setRowCount(0);
            for (Transaksi t : last5) {
                String tanggal = t.getTanggal().format(fmt);
                String kategori = t.isPemasukan() ? "Pemasukan" : "Pengeluaran"; // sementara
                String ket = (t.getKeterangan() == null) ? "" : t.getKeterangan();
                String jumlah = (t.isPemasukan() ? "+" : "-") + MoneyUtil.rupiah(t.getJumlah()).replace("Rp ", "Rp ");
                tableModel.addRow(new Object[]{tanggal, kategori, ket, jumlah});
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal load dashboard: " + e.getMessage());
        }
    }

    // dipanggil setelah tambah/edit/hapus
    public void refreshAll() {
        refreshDashboard();
        if (riwayatPanel != null) riwayatPanel.loadData();
    }

    // ======================================================
    // ===================== COMPONENTS =====================
    // ======================================================
    private class SummaryCard extends JPanel {
        private final JLabel lblValue;

        SummaryCard(String title, String value, Color bg) {
            setLayout(new BorderLayout());
            setBackground(bg);
            setBorder(new EmptyBorder(14, 16, 14, 16));

            JLabel lblTitle = new JLabel(title);
            lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblTitle.setForeground(Color.WHITE);

            lblValue = new JLabel(value);
            lblValue.setFont(new Font("Segoe UI", Font.BOLD, 18));
            lblValue.setForeground(Color.WHITE);

            add(lblTitle, BorderLayout.NORTH);
            add(lblValue, BorderLayout.CENTER);
        }

        void setValue(String v) {
            lblValue.setText(v);
        }
    }

    // Jumlah: warna hijau/merah + rata kanan
    private class AmountRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int col) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

            setHorizontalAlignment(SwingConstants.RIGHT);
            String s = value == null ? "" : value.toString();

            if (!isSelected) {
                if (s.trim().startsWith("+")) c.setForeground(new Color(16, 185, 129));
                else c.setForeground(new Color(239, 68, 68));
                c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(249, 250, 251));
            }
            return c;
        }
    }

    // Kategori “badge-like”
    private class BadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int col) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

            String s = value == null ? "" : value.toString().toLowerCase();

            if (!isSelected) {
                if (s.contains("pemasukan")) {
                    setForeground(new Color(16, 185, 129));
                } else {
                    setForeground(new Color(239, 68, 68));
                }
                c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(249, 250, 251));
            }
            return c;
        }
    }
}
