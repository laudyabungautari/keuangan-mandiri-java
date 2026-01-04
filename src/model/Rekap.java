package model;

public class Rekap {
    private final long totalPemasukan;
    private final long totalPengeluaran;

    public Rekap(long in, long out) {
        this.totalPemasukan = in;
        this.totalPengeluaran = out;
    }

    public long getTotalPemasukan() { return totalPemasukan; }
    public long getTotalPengeluaran() { return totalPengeluaran; }
    public long getBalance() { return totalPemasukan - totalPengeluaran; }
}
