package ui;

import model.Rekap;
import storage.ExportService;
import storage.TransaksiService;
import util.FileUtil;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.time.LocalDate;

public class ExportPanel extends JPanel {

    // Theme
    private final Color COLOR_BG = new Color(248, 249, 250);
    private final Color COLOR_CARD = Color.WHITE;
    private final Color COLOR_TEXT = new Color(33, 37, 41);
    private final Color COLOR_BORDER = new Color(222, 226, 230);

    private final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 20);
    private final Font FONT_LABEL = new Font("Segoe UI", Font.PLAIN, 13);
    private final Font FONT_BTN = new Font("Segoe UI", Font.BOLD, 12);

    public ExportPanel() {
        setLayout(new BorderLayout(12, 12));
        setBackground(COLOR_BG);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COLOR_BG);

        JLabel title = new JLabel("Export Rekap (CSV)");
        title.setFont(FONT_TITLE);
        title.setForeground(COLOR_TEXT);

        JLabel sub = new JLabel("Export ringkasan harian / bulanan / tahunan menjadi file CSV.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(new Color(108, 117, 125));

        JPanel titleWrap = new JPanel();
        titleWrap.setLayout(new BoxLayout(titleWrap, BoxLayout.Y_AXIS));
        titleWrap.setBackground(COLOR_BG);
        titleWrap.add(title);
        titleWrap.add(Box.createVerticalStrut(4));
        titleWrap.add(sub);

        header.add(titleWrap, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // Card
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(COLOR_CARD);

        Border border = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)
        );
        card.setBorder(border);

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.fill = GridBagConstraints.HORIZONTAL;

        // Components
        JComboBox<String> cbMode = new JComboBox<>(new String[]{"Harian", "Bulanan", "Tahunan"});
        cbMode.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JTextField tfTanggal = new JTextField(12); // YYYY-MM-DD
        JTextField tfBulan = new JTextField(8);    // YYYY-MM
        JTextField tfTahun = new JTextField(6);    // YYYY

        tfTanggal.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tfBulan.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tfTahun.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        LocalDate now = LocalDate.now();
        tfTanggal.setText(now.toString());
        tfBulan.setText(now.getYear() + "-" + String.format("%02d", now.getMonthValue()));
        tfTahun.setText(String.valueOf(now.getYear()));

        JLabel lblInfo = new JLabel("Output akan disimpan ke folder: data/");
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblInfo.setForeground(new Color(108, 117, 125));

        // Row helper
        int row = 0;

        // Mode
        addRow(card, g, row++, "Mode Export", cbMode);

        // Tanggal
        addRow(card, g, row++, "Tanggal (Harian)  YYYY-MM-DD", tfTanggal);

        // Bulan
        addRow(card, g, row++, "Bulan (Bulanan)   YYYY-MM", tfBulan);

        // Tahun
        addRow(card, g, row++, "Tahun (Tahunan)   YYYY", tfTahun);

        // Info
        g.gridx = 0;
        g.gridy = row++;
        g.gridwidth = 2;
        g.weightx = 1;
        card.add(lblInfo, g);

        // Buttons
        JButton btnExport = new JButton("Export");
        JButton btnPreview = new JButton("Preview Rekap");
        btnExport.setFont(FONT_BTN);
        btnPreview.setFont(FONT_BTN);
        btnExport.setFocusPainted(false);
        btnPreview.setFocusPainted(false);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setBackground(COLOR_CARD);
        btnRow.add(btnPreview);
        btnRow.add(btnExport);

        g.gridx = 0;
        g.gridy = row++;
        g.gridwidth = 2;
        card.add(btnRow, g);

        add(card, BorderLayout.CENTER);

        // Mode logic: enable/disable input sesuai mode
        Runnable applyMode = () -> {
            String mode = (String) cbMode.getSelectedItem();
            boolean harian = "Harian".equals(mode);
            boolean bulanan = "Bulanan".equals(mode);
            boolean tahunan = "Tahunan".equals(mode);

            tfTanggal.setEnabled(harian);
            tfBulan.setEnabled(bulanan);
            tfTahun.setEnabled(tahunan);
        };
        cbMode.addActionListener(e -> applyMode.run());
        applyMode.run();

        // Preview action
        btnPreview.addActionListener(e -> {
            try {
                Rekap r = getRekapByMode((String) cbMode.getSelectedItem(), tfTanggal.getText(), tfBulan.getText(), tfTahun.getText());
                JOptionPane.showMessageDialog(this,
                        "Preview Rekap\n\n" +
                                "Pemasukan  : " + r.getTotalPemasukan() + "\n" +
                                "Pengeluaran: " + r.getTotalPengeluaran() + "\n" +
                                "Balance    : " + r.getBalance(),
                        "Preview",
                        JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "❌ Preview gagal: " + ex.getMessage());
            }
        });

        // Export action
        btnExport.addActionListener(e -> {
            try {
                FileUtil.ensureDir("data");

                String mode = (String) cbMode.getSelectedItem();

                if ("Harian".equals(mode)) {
                    LocalDate t = LocalDate.parse(tfTanggal.getText().trim());
                    Rekap r = TransaksiService.rekapHarian(t);

                    String out = FileUtil.rekapHarianPath(t.toString());
                    ExportService.exportRekap(out, "Harian", t.toString(),
                            r.getTotalPemasukan(), r.getTotalPengeluaran(), r.getBalance());

                    JOptionPane.showMessageDialog(this, "✅ Export berhasil:\n" + out);

                } else if ("Bulanan".equals(mode)) {
                    String ym = tfBulan.getText().trim();
                    if (ym.length() < 7 || ym.charAt(4) != '-') {
                        throw new IllegalArgumentException("Format bulan harus YYYY-MM (contoh: 2025-12)");
                    }

                    int year = Integer.parseInt(ym.substring(0, 4));
                    int month = Integer.parseInt(ym.substring(5, 7));

                    Rekap r = TransaksiService.rekapBulanan(year, month);

                    String out = FileUtil.rekapBulananPath(ym);
                    ExportService.exportRekap(out, "Bulanan", ym,
                            r.getTotalPemasukan(), r.getTotalPengeluaran(), r.getBalance());

                    JOptionPane.showMessageDialog(this, "✅ Export berhasil:\n" + out);

                } else {
                    int year = Integer.parseInt(tfTahun.getText().trim());
                    Rekap r = TransaksiService.rekapTahunan(year);

                    String out = FileUtil.rekapTahunanPath(year);
                    ExportService.exportRekap(out, "Tahunan", String.valueOf(year),
                            r.getTotalPemasukan(), r.getTotalPengeluaran(), r.getBalance());

                    JOptionPane.showMessageDialog(this, "✅ Export berhasil:\n" + out);
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "❌ Export gagal: " + ex.getMessage());
            }
        });
    }

    private void addRow(JPanel card, GridBagConstraints g, int row, String label, JComponent input) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(new Color(73, 80, 87));

        g.gridx = 0;
        g.gridy = row;
        g.gridwidth = 1;
        g.weightx = 0;
        card.add(lbl, g);

        g.gridx = 1;
        g.weightx = 1;
        card.add(input, g);
    }

    private Rekap getRekapByMode(String mode, String tanggal, String bulan, String tahun) throws Exception {
        if ("Harian".equals(mode)) {
            LocalDate t = LocalDate.parse(tanggal.trim());
            return TransaksiService.rekapHarian(t);
        } else if ("Bulanan".equals(mode)) {
            String ym = bulan.trim();
            int year = Integer.parseInt(ym.substring(0, 4));
            int month = Integer.parseInt(ym.substring(5, 7));
            return TransaksiService.rekapBulanan(year, month);
        } else {
            int year = Integer.parseInt(tahun.trim());
            return TransaksiService.rekapTahunan(year);
        }
    }
}
