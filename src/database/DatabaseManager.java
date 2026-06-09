package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manager untuk koneksi database SQLite.
 * 
 * Database disimpan sebagai file "database.db" di root project.
 * File ini akan dibuat otomatis jika belum ada.
 * 
 * Contoh penggunaan di Repository:
 * 
 *   try (Connection conn = DatabaseManager.getConnection()) {
 *       PreparedStatement ps = conn.prepareStatement("SELECT * FROM events WHERE id = ?");
 *       ps.setString(1, eventId);
 *       ResultSet rs = ps.executeQuery();
 *       while (rs.next()) {
 *           String nama = rs.getString("nama");
 *           double harga = rs.getDouble("harga_dasar");
 *           // ...
 *       }
 *   }
 */
public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:database.db";

    /**
     * Mendapatkan koneksi baru ke database SQLite.
     * 
     * PENTING: Selalu gunakan try-with-resources agar koneksi otomatis ditutup!
     * 
     *   try (Connection conn = DatabaseManager.getConnection()) {
     *       // gunakan conn disini ...
     *   } // conn otomatis ditutup disini
     */
    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL);
        // Aktifkan foreign key support (di SQLite ini harus diaktifkan per koneksi)
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON");
        }
        return conn;
    }

    /**
     * Inisialisasi database: buat semua tabel jika belum ada.
     * Dipanggil sekali saat server pertama kali dijalankan.
     * 
     * Tambahkan CREATE TABLE IF NOT EXISTS untuk setiap tabel yang dibutuhkan.
     */
    public static void initialize() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // =============================================
            // BUAT TABEL-TABEL KALIAN DISINI
            // Gunakan CREATE TABLE IF NOT EXISTS agar aman dijalankan berulang.
            // =============================================

            // Contoh (hapus dan ganti dengan tabel kalian):
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS contoh ("
                + "  id TEXT PRIMARY KEY,"
                + "  nama TEXT NOT NULL,"
                + "  created_at TEXT DEFAULT (datetime('now'))"
                + ")"
            );

            System.out.println("Database berhasil diinisialisasi.");

        } catch (SQLException e) {
            System.err.println("Gagal inisialisasi database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
