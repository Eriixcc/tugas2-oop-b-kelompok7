import server.Server;

import database.DatabaseManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class App {

    public static void main(String[] args) throws Exception {
        // Inisialisasi database (buat tabel jika belum ada)
        DatabaseManager.initialize();

        // Port default 8080, bisa diubah lewat argument: java App 3000
        int port = 8080;
        if (args.length == 1) {
            port = Integer.parseInt(args[0]);
        }

        Server server = new Server(port);

        // =============================================
        // CONTOH ROUTE — Hapus semua contoh di bawah ini
        // dan ganti dengan route API kalian.
        // =============================================

        // --- Contoh 1: GET sederhana ---
        // Coba: curl http://localhost:8080/api/contoh
        server.get("/api/contoh", (req, res) -> {
            List<Map<String, Object>> items = new ArrayList<>();

            Map<String, Object> item1 = new HashMap<>();
            item1.put("id", "CTH-001");
            item1.put("nama", "Contoh pertama");
            items.add(item1);

            Map<String, Object> item2 = new HashMap<>();
            item2.put("id", "CTH-002");
            item2.put("nama", "Contoh kedua");
            items.add(item2);

            res.sendSuccess(items);
        });

        // --- Contoh 2: GET dengan path parameter ---
        // Coba: curl http://localhost:8080/api/contoh/CTH-001
        server.get("/api/contoh/{id}", (req, res) -> {
            String id = req.getPathParam("id");

            Map<String, Object> data = new HashMap<>();
            data.put("id", id);
            data.put("nama", "Item dengan ID " + id);
            res.sendSuccess(data);
        });

        // --- Contoh 3: GET dengan query parameter ---
        // Coba: curl "http://localhost:8080/api/contoh-search?nama=Wayan&kota=Denpasar"
        server.get("/api/contoh-search", (req, res) -> {
            String nama = req.getQueryParam("nama");
            String kota = req.getQueryParam("kota");

            Map<String, Object> data = new HashMap<>();
            data.put("filter_nama", nama);
            data.put("filter_kota", kota);
            data.put("hasil", "Menampilkan data yang cocok...");
            res.sendSuccess(data);
        });

        // --- Contoh 4: POST dengan JSON body ---
        // Coba: curl -X POST http://localhost:8080/api/contoh \
        //        -H "Content-Type: application/json" \
        //        -d '{"nama": "Item Baru", "harga": 50000}'
        server.post("/api/contoh", (req, res) -> {
            Map<String, Object> body = req.getJSON();
            if (body == null) {
                res.sendError(400, "Request body harus berformat JSON (Content-Type: application/json)");
                return;
            }

            String nama = (String) body.get("nama");
            if (nama == null || nama.isEmpty()) {
                res.sendError(400, "Field 'nama' wajib diisi");
                return;
            }

            // Proses data...
            Map<String, Object> created = new HashMap<>();
            created.put("id", "CTH-003");
            created.put("nama", nama);
            created.put("message", "Data berhasil dibuat");
            res.sendCreated(created);
        });

        // --- Contoh 5: PUT dengan path parameter + JSON body ---
        // Coba: curl -X PUT http://localhost:8080/api/contoh/CTH-001 \
        //        -H "Content-Type: application/json" \
        //        -d '{"nama": "Nama Diupdate"}'
        server.put("/api/contoh/{id}", (req, res) -> {
            String id = req.getPathParam("id");
            Map<String, Object> body = req.getJSON();
            if (body == null) {
                res.sendError(400, "Request body harus berformat JSON");
                return;
            }

            Map<String, Object> updated = new HashMap<>();
            updated.put("id", id);
            updated.put("nama", body.get("nama"));
            updated.put("message", "Data berhasil diupdate");
            res.sendSuccess(updated);
        });

        // =============================================
        // DAFTARKAN ROUTE API KALIAN DI BAWAH INI
        //
        // PENTING: Daftarkan route yang lebih spesifik dulu!
        // Contoh yang BENAR:
        //   server.get("/api/events/ringkasan-harga", ...);  // dulu
        //   server.get("/api/events/{id}", ...);              // baru ini
        //
        // Contoh yang SALAH (ringkasan-harga tidak akan pernah tercapai):
        //   server.get("/api/events/{id}", ...);
        //   server.get("/api/events/ringkasan-harga", ...);
        // =============================================



        // Jalankan server
        System.out.printf("Server berjalan di http://localhost:%d%n", port);
        System.out.println("Tekan Ctrl+C untuk menghentikan server.\n");
        System.out.println("Contoh pengujian dengan curl:");
        System.out.printf("  curl http://localhost:%d/api/contoh%n", port);
        System.out.printf("  curl http://localhost:%d/api/contoh/CTH-001%n", port);
        System.out.printf("  curl -X POST http://localhost:%d/api/contoh -H \"Content-Type: application/json\" -d '{\"nama\": \"Test\"}'%n%n", port);
        server.start();
    }
}

// =============================================
// CARA MENJALANKAN
//
// 1. Pastikan library JAR ada di folder lib/:
//    lib/
//    ├── jackson-annotations-2.13.3.jar
//    ├── jackson-core-2.13.3.jar
//    ├── jackson-databind-2.13.3.jar
//    └── sqlite-jdbc-3.49.1.0.jar
//
// 2. Kompilasi (jalankan dari folder src/):
//
//    Linux/Mac:
//      javac -cp ".:../lib/*" App.java server/*.java database/*.java
//
//    Windows:
//      javac -cp ".;../lib/*" App.java server/*.java database/*.java
//
// 3. Jalankan (dari folder src/):
//
//    Linux/Mac:
//      java -cp ".:../lib/*" App
//
//    Windows:
//      java -cp ".;../lib/*" App
//
// 4. Test dengan curl atau Postman di http://localhost:8080
// =============================================
