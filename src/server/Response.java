package server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Wrapper untuk HTTP response.
 * Menyediakan method untuk mengirim response JSON dengan format standar.
 * 
 * Format response standar:
 *   Sukses: {"status": "success", "data": {...}}
 *   Error:  {"status": "error", "message": "..."}
 */
public class Response {

    private final HttpExchange httpExchange;
    private boolean sent;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public Response(HttpExchange httpExchange) {
        this.httpExchange = httpExchange;
        this.sent = false;
    }

    /**
     * Kirim response JSON dengan status code dan body tertentu.
     * Body akan diserialisasi menjadi JSON menggunakan Jackson.
     * 
     * @param status HTTP status code (200, 201, 400, 404, 500, dll.)
     * @param body   Object yang akan diserialisasi ke JSON (bisa Map, List, atau POJO)
     */
    public void json(int status, Object body) {
        if (sent) return;
        try {
            String json = objectMapper.writeValueAsString(body);
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

            httpExchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            httpExchange.sendResponseHeaders(status, bytes.length);

            OutputStream os = httpExchange.getResponseBody();
            os.write(bytes);
            os.flush();
            os.close();
        } catch (Exception e) {
            System.err.println("Gagal mengirim response: " + e.getMessage());
            e.printStackTrace();
        } finally {
            httpExchange.close();
            sent = true;
        }
    }

    /**
     * Kirim response sukses (HTTP 200 OK).
     * Format: {"status": "success", "data": ...}
     * 
     * @param data Object yang akan dikirim sebagai "data" (bisa Map, List, atau POJO)
     * 
     * Contoh:
     *   Map<String, Object> event = new HashMap<>();
     *   event.put("id", "EVT-001");
     *   event.put("nama", "Konser Musik");
     *   res.sendSuccess(event);
     *   // → {"status":"success","data":{"id":"EVT-001","nama":"Konser Musik"}}
     */
    public void sendSuccess(Object data) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "success");
        body.put("data", data);
        json(200, body);
    }

    /**
     * Kirim response berhasil dibuat (HTTP 201 Created).
     * Format: {"status": "success", "data": ...}
     * 
     * Gunakan ini setelah berhasil membuat resource baru (POST).
     */
    public void sendCreated(Object data) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "success");
        body.put("data", data);
        json(201, body);
    }

    /**
     * Kirim response error.
     * Format: {"status": "error", "message": "..."}
     * 
     * @param status  HTTP status code (400, 404, 500, dll.)
     * @param message Pesan error yang informatif
     * 
     * Contoh:
     *   res.sendError(404, "Event dengan ID EVT-999 tidak ditemukan");
     *   res.sendError(400, "Field 'nama' wajib diisi");
     */
    public void sendError(int status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "error");
        body.put("message", message);
        json(status, body);
    }

    /**
     * Cek apakah response sudah dikirim.
     * Response hanya bisa dikirim sekali per request.
     */
    public boolean isSent() {
        return sent;
    }
}
