package util;

import java.nio.file.Files;
import java.nio.file.Paths;

public class FileUtil {
    public static void ensureDir(String dir) {
        try {
            if (!Files.exists(Paths.get(dir))) Files.createDirectories(Paths.get(dir));
        } catch (Exception e) {
            throw new RuntimeException("Gagal membuat folder: " + dir + " | " + e.getMessage());
        }
    }

    public static String rekapHarianPath(String dateISO) { return "data/rekap_harian_" + dateISO + ".csv"; }
    public static String rekapBulananPath(String yearMonth) { return "data/rekap_bulanan_" + yearMonth + ".csv"; }
    public static String rekapTahunanPath(int year) { return "data/rekap_tahunan_" + year + ".csv"; }
}
