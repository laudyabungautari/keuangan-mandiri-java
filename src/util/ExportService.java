package storage;

import java.io.BufferedWriter;
import java.io.FileWriter;

public class ExportService {

    public static void exportRekap(String path, String mode, String periode,
                                   long pemasukan, long pengeluaran, long balance) throws Exception {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
            bw.write("mode,periode,total_pemasukan,total_pengeluaran,balance");
            bw.newLine();
            bw.write(mode + "," + periode + "," + pemasukan + "," + pengeluaran + "," + balance);
            bw.newLine();
        }
    }
}
