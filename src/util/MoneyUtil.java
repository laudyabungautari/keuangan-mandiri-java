package util;

public class MoneyUtil {

    // Format Rp 1.234.567
    public static String rupiah(long n) {
        return "Rp " + String.format("%,d", n).replace(',', '.');
    }

    // Parse input angka:
    // "10000" -> 10000
    // "10.000" -> 10000
    // "Rp 10.000" -> 10000
    public static int parse(String s) {
        if (s == null) throw new IllegalArgumentException("Jumlah kosong");

        String clean = s.trim()
                .replace("Rp", "")
                .replace("rp", "")
                .replace(".", "")
                .replace(",", "")
                .replace(" ", "");

        if (clean.isEmpty()) throw new IllegalArgumentException("Jumlah kosong");
        return Integer.parseInt(clean);
    }

    // Biar kompatibel sama kode kamu yang lama (parseAmount)
    public static int parseAmount(String s) {
        return parse(s);
    }
}
