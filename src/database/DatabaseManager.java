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
                "CREATE TABLE IF NOT EXISTS users ("
                + "  id TEXT PRIMARY KEY,"
                + "  name TEXT NOT NULL,"
                + "  email TEXT NOT NULL UNIQUE,"
                + "  phone TEXT,"
                + "  role TEXT DEFAULT 'buyer',"
                + "  created_at  TEXT DEFAULT (datetime('now'))"
                + ")"
            );

             stmt.execute(
                "CREATE TABLE IF NOT EXISTS venues ("
                + "  id              TEXT PRIMARY KEY," 
                + "  name            TEXT NOT NULL," 
                + "  address         TEXT NOT NULL,"
                + "  max_capacity    INTEGER NOT NULL," 
                + "  created_at      TEXT DEFAULT (datetime('now'))" 
                + ");"
            );

            stmt.execute(
                "CREATE TABLE IF NOT EXISTS events (" 
                + "  id TEXT PRIMARY KEY," 
                + "  type   TEXT NOT NULL," 
                + "  name   TEXT NOT NULL," 
                + "  venue_id        TEXT NOT NULL," 
                + "  organizer_id    TEXT NOT NULL,"  
                + "  date            TEXT NOT NULL,"
                + "  base_price      REAL NOT NULL," 
                + "  created_at      TEXT DEFAULT (datetime('now'))," 
                + "  FOREIGN KEY (venue_id) REFERENCES venues(id)," 
                + "  FOREIGN KEY (organizer_id) REFERENCES users(id)" 
                + ");"
            );

            stmt.execute(
                "CREATE TABLE IF NOT EXISTS capacities ("
                + "  event_id TEXT NOT NULL,"
                + "  category TEXT NOT NULL,"
                + "  total_capacity INTEGER NOT NULL,"
                + "  remaining_capacity INTEGER NOT NULL,"
                + "  PRIMARY KEY (event_id, category),"
                + "  FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE"
                + ");"
            );

            stmt.execute(
                "CREATE TABLE IF NOT EXISTS tickets ("
                + "  id TEXT PRIMARY KEY,"
                + "  event_id TEXT NOT NULL,"
                + "  category TEXT NOT NULL,"
                + "  price REAL NOT NULL,"
                + "  status TEXT DEFAULT 'valid'," 
                + "  created_at TEXT DEFAULT (datetime('now')),"
                + "  FOREIGN KEY (event_id) REFERENCES events(id)"
                + ");"
            );

            stmt.execute(
                "CREATE TABLE IF NOT EXISTS transactions ("
                + "  id TEXT PRIMARY KEY,"
                + "  user_id TEXT NOT NULL,"
                + "  ticket_id TEXT NOT NULL,"
                + "  type TEXT NOT NULL," 
                + "  amount REAL NOT NULL,"
                + "  created_at TEXT DEFAULT (datetime('now')),"
                + "  FOREIGN KEY (user_id) REFERENCES users(id),"
                + "  FOREIGN KEY (ticket_id) REFERENCES tickets(id)"
                + ");"
            );

            System.out.println("Database berhasil diinisialisasi.");

        } catch (SQLException e) {
            System.err.println("Gagal inisialisasi database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
