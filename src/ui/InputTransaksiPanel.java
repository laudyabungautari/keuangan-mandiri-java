package ui;

import storage.TransaksiService;
import util.MoneyUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.LocalDate;

public class InputTransaksiPanel extends JPanel {

    private final DashboardFrame frame;

    private JTextField tfTanggal;
    private JComboBox<String> cbTipe;
    private JTextField tfJumlah;
    private JTextArea taKeterangan;

    public InputTransaksiPanel(DashboardFrame frame) {
        this.frame = frame;

        setLayout(new BorderLayout());
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(18, 20, 18, 20));

        JPanel wrap = new JPanel(new BorderLayout(12, 12));
        wrap.setOpaque(false);

        wrap.add(buildHeader(), BorderLayout.NORTH);
        wrap.add(Theme.card(buildForm()), BorderLayout.CENTER);

        add(wrap, BorderLayout.CENTER);
    }

    // ===== HEADER =====
    private JPanel buildHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);

        JLabel title = new JLabel("Tambah Transaksi");
        title.setFont(Theme.H1);
        title.setForeground(Theme.TEXT);

        JLabel sub = new JLabel("Catat pemasukan atau pengeluaran keuanganmu (biar jajan tetap aman 😄).");
        sub.setFont(Theme.BODY);
        sub.setForeground(Theme.MUTED);

        header.add(title);
        header.add(Box.createVerticalStrut(6));
        header.add(sub);

        return header;
    }

    // ===== FORM =====
    private JPanel buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.CARD);
        form.setBorder(new EmptyBorder(8, 6, 6, 6));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(10, 10, 10, 10);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;

        // ---- Row 0: Tanggal
        g.gridx = 0; g.gridy = 0; g.weightx = 0;
        form.add(label("Tanggal (YYYY-MM-DD)"), g);

        g.gridx = 1; g.weightx = 1;
        tfTanggal = inputText(LocalDate.now().toString());
        form.add(tfTanggal, g);

        // ---- Row 1: Tipe
        g.gridx = 0; g.gridy++; g.weightx = 0;
        form.add(label("Tipe Transaksi"), g);

        g.gridx = 1; g.weightx = 1;
        cbTipe = new JComboBox<>(new String[]{"pemasukan", "pengeluaran"});
        cbTipe.setFont(Theme.BODY);
        form.add(cbTipe, g);

        // ---- Row 2: Jumlah
        g.gridx = 0; g.gridy++; g.weightx = 0;
        form.add(label("Jumlah"), g);

        g.gridx = 1; g.weightx = 1;
        tfJumlah = inputText("");
        form.add(tfJumlah, g);

        // Enter = simpan
        tfJumlah.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) simpan();
            }
        });

        // ---- Row 3: Keterangan
        g.gridx = 0; g.gridy++; g.weightx = 0;
        g.anchor = GridBagConstraints.NORTH;
        form.add(label("Keterangan"), g);

        g.gridx = 1; g.weightx = 1;
        taKeterangan = new JTextArea(4, 24);
        taKeterangan.setFont(Theme.BODY);
        taKeterangan.setLineWrap(true);
        taKeterangan.setWrapStyleWord(true);

        JScrollPane sp = new JScrollPane(taKeterangan);
        sp.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        form.add(sp, g);

        // ---- Row 4: Buttons
        g.gridx = 1; g.gridy++;
        g.weightx = 1;
        g.anchor = GridBagConstraints.EAST;

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setBackground(Theme.CARD);

        JButton btnReset = Theme.softButton("Reset");
        btnReset.addActionListener(e -> resetForm());

        JButton btnSimpan = Theme.primaryButton("Simpan Transaksi");
        btnSimpan.addActionListener(e -> simpan());

        actions.add(btnReset);
        actions.add(btnSimpan);

        form.add(actions, g);

        return form;
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.BODY_BOLD);
        l.setForeground(Theme.TEXT);
        return l;
    }

    private JTextField inputText(String value) {
        JTextField tf = new JTextField(value);
        tf.setFont(Theme.BODY);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                new EmptyBorder(8, 10, 8, 10)
        ));
        return tf;
    }

    // ===== LOGIC =====
    private void simpan() {
        try {
            // Tanggal
            LocalDate tanggal;
            try {
                tanggal = LocalDate.parse(tfTanggal.getText().trim());
            } catch (Exception e) {
                error("Format tanggal salah.\nContoh: 2025-12-22");
                tfTanggal.requestFocus();
                return;
            }

            // Tipe
            String tipe = cbTipe.getSelectedItem().toString();

            // Jumlah
            int jumlah;
            try {
                jumlah = MoneyUtil.parse(tfJumlah.getText().trim());
            } catch (Exception e) {
                error("Jumlah harus angka.\nContoh: 10000");
                tfJumlah.requestFocus();
                return;
            }

            if (jumlah <= 0) {
                error("Jumlah harus lebih dari 0");
                tfJumlah.requestFocus();
                return;
            }

            // Keterangan
            String ket = taKeterangan.getText().trim();

            // Simpan
            TransaksiService.tambah(tanggal, tipe, jumlah, ket);

            JOptionPane.showMessageDialog(this, "✅ Transaksi berhasil disimpan");

            resetForm();

            // Update dashboard + riwayat
            if (frame != null) frame.refreshAll();

        } catch (Exception e) {
            error(e.getMessage());
        }
    }

    private void resetForm() {
        tfTanggal.setText(LocalDate.now().toString());
        cbTipe.setSelectedIndex(0);
        tfJumlah.setText("");
        taKeterangan.setText("");
        tfJumlah.requestFocus();
    }

    private void error(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Validasi Gagal", JOptionPane.WARNING_MESSAGE);
    }
}
