package ui;

import util.MoneyUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;

public class EditTransaksiDialog extends JDialog {

    // THEME
    private final Color BG = new Color(245, 247, 250);
    private final Color CARD = Color.WHITE;
    private final Color TEXT = new Color(31, 41, 55);
    private final Color MUTED = new Color(107, 114, 128);
    private final Color PRIMARY = new Color(99, 102, 241);

    private boolean saved = false;

    private final int id;
    private JTextField tfTanggal;
    private JComboBox<String> cbTipe;
    private JTextField tfJumlah;
    private JTextField tfKet;

    private LocalDate outTanggal;
    private String outTipe;
    private int outJumlah;
    private String outKet;

    public EditTransaksiDialog(Window owner, int id, String tanggalOld, String tipeOld, String jumlahOld, String ketOld) {
        super(owner, "Edit Transaksi", ModalityType.APPLICATION_MODAL);
        this.id = id;

        setSize(460, 360);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildForm(tanggalOld, tipeOld, jumlahOld, ketOld), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(BG);
        header.setBorder(new EmptyBorder(18, 18, 10, 18));

        JLabel title = new JLabel("Edit Transaksi");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT);

        JLabel sub = new JLabel("Ubah data transaksi lalu klik Simpan.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(MUTED);

        header.add(title);
        header.add(Box.createVerticalStrut(6));
        header.add(sub);
        return header;
    }

    private JPanel buildForm(String tanggalOld, String tipeOld, String jumlahOld, String ketOld) {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(CARD);
        card.setBorder(new EmptyBorder(16, 16, 16, 16));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.fill = GridBagConstraints.HORIZONTAL;

        // ID (readonly)
        g.gridx = 0; g.gridy = 0; g.weightx = 0;
        card.add(label("ID"), g);

        g.gridx = 1; g.weightx = 1;
        JLabel lblId = new JLabel(String.valueOf(id));
        lblId.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblId.setForeground(TEXT);
        card.add(lblId, g);

        // Tanggal
        g.gridx = 0; g.gridy++;
        card.add(label("Tanggal (YYYY-MM-DD)"), g);

        g.gridx = 1;
        tfTanggal = new JTextField(tanggalOld);
        card.add(tfTanggal, g);

        // Tipe
        g.gridx = 0; g.gridy++;
        card.add(label("Tipe"), g);

        g.gridx = 1;
        cbTipe = new JComboBox<>(new String[]{"pemasukan", "pengeluaran"});
        cbTipe.setSelectedItem(tipeOld);
        card.add(cbTipe, g);

        // Jumlah
        g.gridx = 0; g.gridy++;
        card.add(label("Jumlah"), g);

        g.gridx = 1;
        int cleanAmount = 0;
        try { cleanAmount = MoneyUtil.parse(jumlahOld); } catch (Exception ignored) {}
        tfJumlah = new JTextField(String.valueOf(cleanAmount));
        card.add(tfJumlah, g);

        // Keterangan
        g.gridx = 0; g.gridy++;
        card.add(label("Keterangan"), g);

        g.gridx = 1;
        tfKet = new JTextField(ketOld == null ? "" : ketOld);
        card.add(tfKet, g);

        return card;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(BG);
        footer.setBorder(new EmptyBorder(12, 18, 18, 18));

        JLabel hint = new JLabel("Tips: Jumlah boleh ketik 10000 atau Rp 10.000");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hint.setForeground(MUTED);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setBackground(BG);

        JButton btnCancel = new JButton("Batal");
        btnCancel.putClientProperty("JButton.buttonType", "roundRect");
        btnCancel.setFocusPainted(false);

        JButton btnSave = new JButton("Simpan");
        btnSave.putClientProperty("JButton.buttonType", "roundRect");
        btnSave.setBackground(PRIMARY);
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> onSave());

        actions.add(btnCancel);
        actions.add(btnSave);

        footer.add(hint, BorderLayout.WEST);
        footer.add(actions, BorderLayout.EAST);
        return footer;
    }

    private JLabel label(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(TEXT);
        return l;
    }

    private void onSave() {
        try {
            LocalDate tgl = LocalDate.parse(tfTanggal.getText().trim());
            String tipe = cbTipe.getSelectedItem().toString().trim();
            int jumlah = MoneyUtil.parse(tfJumlah.getText().trim());
            String ket = tfKet.getText().trim();

            if (jumlah <= 0) throw new IllegalArgumentException("Jumlah harus lebih dari 0");

            outTanggal = tgl;
            outTipe = tipe;
            outJumlah = jumlah;
            outKet = ket;

            saved = true;
            dispose();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "❌ " + e.getMessage(), "Validasi Gagal", JOptionPane.WARNING_MESSAGE);
        }
    }

    public boolean isSaved() { return saved; }

    public LocalDate getTanggal() { return outTanggal; }
    public String getTipe() { return outTipe; }
    public int getJumlah() { return outJumlah; }
    public String getKeterangan() { return outKet; }
}
