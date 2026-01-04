package app;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class InitData {

    public static final String DATA_DIR = "data";
    public static final String TRANSAKSI_FILE = DATA_DIR + "/transaksi.csv";
    public static final String HEADER = "id,tanggal,tipe,jumlah,keterangan";

    public static void init() {
        try {
            // 1) Buat folder data/ kalau belum ada
            Path dir = Paths.get(DATA_DIR);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }

            // 2) Buat file transaksi.csv kalau belum ada
            Path file = Paths.get(TRANSAKSI_FILE);
            if (!Files.exists(file)) {
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(TRANSAKSI_FILE))) {
                    bw.write(HEADER);
                    bw.newLine();
                }
                System.out.println("✅ File dibuat: " + TRANSAKSI_FILE);
            } else {
                // 3) Kalau file sudah ada tapi kosong, tulis header
                long size = Files.size(file);
                if (size == 0) {
                    try (BufferedWriter bw = new BufferedWriter(new FileWriter(TRANSAKSI_FILE))) {
                        bw.write(HEADER);
                        bw.newLine();
                    }
                    System.out.println("✅ File kosong, header ditambahkan: " + TRANSAKSI_FILE);
                } else {
                    System.out.println("✅ File sudah ada: " + TRANSAKSI_FILE);
                }
            }

        } catch (Exception e) {
            System.out.println("❌ Init data gagal: " + e.getMessage());
        }
    }
}
