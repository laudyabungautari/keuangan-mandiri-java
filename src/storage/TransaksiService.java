package storage;

import app.InitData;
import model.Rekap;
import model.Transaksi;
import util.CsvUtil;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransaksiService {

    // ====== Ambil semua transaksi dari CSV ======
    public static List<Transaksi> getAll() throws IOException {
        List<List<String>> rows = CsvUtil.readAll(InitData.TRANSAKSI_FILE);
        List<Transaksi> list = new ArrayList<>();

        // row 0 = header
        for (int i = 1; i < rows.size(); i++) {
            List<String> r = rows.get(i);
            if (r.size() < 4) continue; // minimal id,tanggal,tipe,jumlah

            // pastikan kolom keterangan ada
            while (r.size() < 5) r.add("");

            try {
                list.add(Transaksi.fromCsvColumns(r));
            } catch (Exception ignored) {
                // skip baris rusak
            }
        }
        return list;
    }

    // ====== Tambah transaksi baru (auto id) ======
    public static void tambah(LocalDate tanggal, String tipe, int jumlah, String keterangan) throws IOException {
        if (tanggal == null) throw new IllegalArgumentException("Tanggal tidak boleh kosong");
        if (!isValidTipe(tipe)) throw new IllegalArgumentException("Tipe harus 'pemasukan' atau 'pengeluaran'");
        if (jumlah <= 0) throw new IllegalArgumentException("Jumlah harus lebih dari 0");

        int nextId = getNextId();
        Transaksi t = new Transaksi(
                nextId,
                tanggal,
                tipe.toLowerCase(),
                jumlah,
                keterangan == null ? "" : keterangan.trim()
        );

        CsvUtil.appendRow(InitData.TRANSAKSI_FILE, t.toCsvColumns());
    }

    private static boolean isValidTipe(String tipe) {
        if (tipe == null) return false;
        String x = tipe.toLowerCase();
        return x.equals("pemasukan") || x.equals("pengeluaran");
    }

    // ====== Auto ID ======
    private static int getNextId() throws IOException {
        List<Transaksi> all = getAll();
        int max = 0;
        for (Transaksi t : all) {
            if (t.getId() > max) max = t.getId();
        }
        return max + 1;
    }

    // ====== Rekap range tanggal (inclusive) ======
    public static Rekap rekapRange(LocalDate from, LocalDate to) throws IOException {
        if (from == null || to == null) throw new IllegalArgumentException("Tanggal range tidak boleh kosong");

        long pemasukan = 0;
        long pengeluaran = 0;

        for (Transaksi t : getAll()) {
            LocalDate d = t.getTanggal();
            if (d.isBefore(from) || d.isAfter(to)) continue;

            if (t.isPemasukan()) pemasukan += t.getJumlah();
            if (t.isPengeluaran()) pengeluaran += t.getJumlah();
        }

        return new Rekap(pemasukan, pengeluaran);
    }

    public static Rekap rekapHarian(LocalDate tanggal) throws IOException {
        return rekapRange(tanggal, tanggal);
    }

    public static Rekap rekapBulanan(int tahun, int bulan) throws IOException {
        LocalDate from = LocalDate.of(tahun, bulan, 1);
        LocalDate to = from.withDayOfMonth(from.lengthOfMonth());
        return rekapRange(from, to);
    }

    public static Rekap rekapTahunan(int tahun) throws IOException {
        return rekapRange(LocalDate.of(tahun, 1, 1), LocalDate.of(tahun, 12, 31));
    }

    // ====== Hapus transaksi berdasarkan ID ======
    public static boolean hapusById(int id) throws IOException {
        List<List<String>> rows = CsvUtil.readAll(InitData.TRANSAKSI_FILE);
        if (rows.size() <= 1) return false;

        List<List<String>> newRows = new ArrayList<>();
        newRows.add(rows.get(0)); // header

        boolean deleted = false;
        for (int i = 1; i < rows.size(); i++) {
            List<String> r = rows.get(i);
            if (r.size() < 1) continue;

            int rid;
            try {
                rid = Integer.parseInt(r.get(0));
            } catch (Exception e) {
                continue; // skip baris rusak
            }

            if (rid == id) {
                deleted = true;
            } else {
                newRows.add(r);
            }
        }

        if (deleted) {
            CsvUtil.writeAll(InitData.TRANSAKSI_FILE, newRows);
        }
        return deleted;
    }

    // ====== UPDATE transaksi berdasarkan ID (untuk fitur Edit) ======
    public static boolean updateById(int id, LocalDate tanggal, String tipe, int jumlah, String keterangan) throws IOException {
        if (tanggal == null) throw new IllegalArgumentException("Tanggal tidak boleh kosong");
        if (!isValidTipe(tipe)) throw new IllegalArgumentException("Tipe harus 'pemasukan' atau 'pengeluaran'");
        if (jumlah <= 0) throw new IllegalArgumentException("Jumlah harus lebih dari 0");

        List<List<String>> rows = CsvUtil.readAll(InitData.TRANSAKSI_FILE);
        if (rows.size() <= 1) return false;

        boolean updated = false;

        for (int i = 1; i < rows.size(); i++) {
            List<String> r = rows.get(i);
            if (r.size() < 1) continue;

            int rid;
            try {
                rid = Integer.parseInt(r.get(0));
            } catch (Exception e) {
                continue;
            }

            if (rid == id) {
                // pastikan kolom lengkap id,tanggal,tipe,jumlah,keterangan
                while (r.size() < 5) r.add("");

                r.set(1, tanggal.toString());
                r.set(2, tipe.toLowerCase());
                r.set(3, String.valueOf(jumlah));
                r.set(4, keterangan == null ? "" : keterangan.trim());

                updated = true;
                break;
            }
        }

        if (updated) {
            CsvUtil.writeAll(InitData.TRANSAKSI_FILE, rows); // simpan ulang seluruh file
        }

        return updated;
    }
}
