package app;

import ui.DashboardFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {

    public static void main(String[] args) {

        // 1. Set Look & Feel agar tampilan lebih modern
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            // Jika gagal, gunakan default Look & Feel
        }

        // 2. Inisialisasi data (folder data + transaksi.csv)
        InitData.init();

        // 3. Jalankan aplikasi GUI
        SwingUtilities.invokeLater(() -> {
            new DashboardFrame().setVisible(true);
        });
    }
}
