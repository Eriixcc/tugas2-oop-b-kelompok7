package server;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Wrapper untuk HTTP request.
 * Menyediakan method untuk mengakses method, path, body, JSON, path parameter, dan query parameter.
 */
public class Request {

    private final HttpExchange httpExchange;
    private String rawBody;
    private Map<String, Object> jsonBody;
    private Map<String, String> pathParams;
    private Map<String, String> queryParams;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public Request(HttpExchange httpExchange) {
        this.httpExchange = httpExchange;
        this.pathParams = new HashMap<>();
    }

    /**
     * Mendapatkan HTTP method dari request.
     * @return "GET", "POST", "PUT", atau "DELETE"
     */
    public String getMethod() {
        return httpExchange.getRequestMethod().toUpperCase();
    }

    /**
     * Mendapatkan path dari request (tanpa query string).
     * Contoh: untuk URL "/api/events?tipe=konser", return "/api/events"
     */
    public String getPath() {
        return httpExchange.getRequestURI().getPath();
    }

    /**
     * Mendapatkan raw body dari request sebagai String.
     * Berguna untuk request POST/PUT yang mengirim data.
     */
    public String getBody() {
        if (rawBody == null) {
            rawBody = new BufferedReader(
                new InputStreamReader(httpExchange.getRequestBody(), StandardCharsets.UTF_8)
            ).lines().collect(Collectors.joining("\n"));
        }
        return rawBody;
    }

    /**
     * Parse body request sebagai JSON dan kembalikan sebagai Map.
     * 
     * @return Map berisi key-value dari JSON body, atau null jika Content-Type bukan application/json
     * @throws Exception jika JSON tidak valid
     * 
     * Contoh penggunaan:
     *   Map<String, Object> body = req.getJSON();
     *   String nama = (String) body.get("nama");
     *   int harga = ((Number) body.get("hargaDasar")).intValue();
     */
    public Map<String, Object> getJSON() throws Exception {
        String contentType = httpExchange.getRequestHeaders().getFirst("Content-Type");
        if (contentType == null || !contentType.toLowerCase().contains("application/json")) {
            return null;
        }

        if (jsonBody == null) {
            jsonBody = objectMapper.readValue(getBody(), new TypeReference<Map<String, Object>>() {});
        }
        return jsonBody;
    }

    /**
     * Mendapatkan path parameter berdasarkan nama.
     * Path parameter didefinisikan di route pattern menggunakan kurung kurawal.
     * 
     * Contoh:
     *   Route:   server.get("/api/events/{id}", handler);
     *   Request: GET /api/events/EVT-005
     *   
     *   req.getPathParam("id") → "EVT-005"
     */
    public String getPathParam(String name) {
        return pathParams.get(name);
    }

    /**
     * Mendapatkan query parameter berdasarkan nama.
     * 
     * Contoh:
     *   Request: GET /api/events?tipe=konser&tanggalDari=2026-07-01
     *   
     *   req.getQueryParam("tipe")        → "konser"
     *   req.getQueryParam("tanggalDari") → "2026-07-01"
     *   req.getQueryParam("xxx")         → null
     */
    public String getQueryParam(String name) {
        if (queryParams == null) {
            queryParams = parseQueryParams();
        }
        return queryParams.get(name);
    }

    /**
     * Mendapatkan semua query parameter sebagai Map.
     */
    public Map<String, String> getQueryParams() {
        if (queryParams == null) {
            queryParams = parseQueryParams();
        }
        return new HashMap<>(queryParams);
    }

    // ================================================================
    // Method internal — digunakan oleh class Server saat routing.
    // Tidak perlu dipanggil langsung oleh mahasiswa.
    // ================================================================

    void setPathParams(Map<String, String> params) {
        this.pathParams = params;
    }

    private Map<String, String> parseQueryParams() {
        Map<String, String> params = new HashMap<>();
        String query = httpExchange.getRequestURI().getQuery();
        if (query == null || query.isEmpty()) {
            return params;
        }
        for (String param : query.split("&")) {
            String[] pair = param.split("=", 2);
            if (pair.length == 2) {
                params.put(pair[0], pair[1]);
            } else if (pair.length == 1) {
                params.put(pair[0], "");
            }
        }
        return params;
    }
}
