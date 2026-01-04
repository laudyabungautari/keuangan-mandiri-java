package model;

import java.time.LocalDate;
import java.util.List;
import model.Rekap;


public class Transaksi {
    private int id;
    private LocalDate tanggal;
    private String tipe;       // "pemasukan" atau "pengeluaran"
    private int jumlah;
    private String keterangan;

    public Transaksi(int id, LocalDate tanggal, String tipe, int jumlah, String keterangan) {
        this.id = id;
        this.tanggal = tanggal;
        this.tipe = tipe;
        this.jumlah = jumlah;
        this.keterangan = keterangan;
    }

    // ===== Getter & Setter =====
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getTanggal() {
        return tanggal;
    }

    public void setTanggal(LocalDate tanggal) {
        this.tanggal = tanggal;
    }

    public String getTipe() {
        return tipe;
    }

    public void setTipe(String tipe) {
        this.tipe = tipe;
    }

    public int getJumlah() {
        return jumlah;
    }

    public void setJumlah(int jumlah) {
        this.jumlah = jumlah;
    }

    public String getKeterangan() {
        return keterangan;
    }

    public void setKeterangan(String keterangan) {
        this.keterangan = keterangan;
    }

    // ===== Helper validasi tipe =====
    public boolean isPemasukan() {
        return "pemasukan".equalsIgnoreCase(tipe);
    }

    public boolean isPengeluaran() {
        return "pengeluaran".equalsIgnoreCase(tipe);
    }

    // ===== Convert objek ke list kolom CSV =====
    public List<String> toCsvColumns() {
        return List.of(
                String.valueOf(id),
                tanggal.toString(),   // format: YYYY-MM-DD
                tipe,
                String.valueOf(jumlah),
                keterangan == null ? "" : keterangan
        );
    }

    // ===== Buat objek dari list kolom CSV =====
    // cols: [id, tanggal, tipe, jumlah, keterangan]
    public static Transaksi fromCsvColumns(List<String> cols) {
        int id = Integer.parseInt(cols.get(0));
        LocalDate tanggal = LocalDate.parse(cols.get(1));
        String tipe = cols.get(2);
        int jumlah = Integer.parseInt(cols.get(3));
        String ket = cols.size() >= 5 ? cols.get(4) : "";
        return new Transaksi(id, tanggal, tipe, jumlah, ket);
    }

    @Override
    public String toString() {
        return "Transaksi{" +
                "id=" + id +
                ", tanggal=" + tanggal +
                ", tipe='" + tipe + '\'' +
                ", jumlah=" + jumlah +
                ", keterangan='" + keterangan + '\'' +
                '}';
    }
}
