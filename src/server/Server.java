package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HTTP Server sederhana dengan fitur routing.
 * 
 * Cara penggunaan:
 *   Server server = new Server(8080);
 * 
 *   server.get("/api/events", (req, res) -> { ... });
 *   server.get("/api/events/{id}", (req, res) -> { ... });
 *   server.post("/api/events", (req, res) -> { ... });
 *   server.put("/api/events/{id}", (req, res) -> { ... });
 * 
 *   server.start();
 * 
 * PENTING: Daftarkan route yang lebih spesifik terlebih dahulu.
 * Contoh yang BENAR:
 *   server.get("/api/events/ringkasan-harga", ...);  // spesifik — daftarkan dulu
 *   server.get("/api/events/{id}", ...);              // parameter — daftarkan setelahnya
 * 
 * Contoh yang SALAH:
 *   server.get("/api/events/{id}", ...);              // ini akan menangkap "ringkasan-harga" sebagai {id}
 *   server.get("/api/events/ringkasan-harga", ...);   // tidak akan pernah tercapai
 */
public class Server {

    private final HttpServer httpServer;
    private final List<Route> routes;
    private final int port;

    public Server(int port) throws Exception {
        this.port = port;
        this.httpServer = HttpServer.create(new InetSocketAddress(port), 128);
        this.routes = new ArrayList<>();
    }

    // ================================================================
    // Registrasi Route
    // Gunakan method-method ini untuk mendaftarkan handler pada route tertentu.
    // Pattern bisa mengandung path parameter dalam kurung kurawal: {namaParam}
    // ================================================================

    /** Daftarkan handler untuk HTTP GET */
    public void get(String pattern, RouteHandler handler) {
        routes.add(new Route("GET", pattern, handler));
    }

    /** Daftarkan handler untuk HTTP POST */
    public void post(String pattern, RouteHandler handler) {
        routes.add(new Route("POST", pattern, handler));
    }

    /** Daftarkan handler untuk HTTP PUT */
    public void put(String pattern, RouteHandler handler) {
        routes.add(new Route("PUT", pattern, handler));
    }

    /** Daftarkan handler untuk HTTP DELETE */
    public void delete(String pattern, RouteHandler handler) {
        routes.add(new Route("DELETE", pattern, handler));
    }

    /** Jalankan server. Panggil ini setelah semua route didaftarkan. */
    public void start() {
        httpServer.createContext("/", new MainHandler());
        httpServer.start();
    }

    // ================================================================
    // Implementasi internal — tidak perlu diubah oleh mahasiswa
    // ================================================================

    /**
     * Handler utama yang mencocokkan setiap request dengan route yang terdaftar.
     */
    private class MainHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) {
            Request req = new Request(exchange);
            Response res = new Response(exchange);

            String method = req.getMethod();
            String path = req.getPath();

            // Log request masuk
            System.out.printf("[%s] %s%n", method, path);

            try {
                // Cari route yang cocok (method + path pattern)
                Route matchedRoute = null;
                Map<String, String> pathParams = new HashMap<>();

                for (Route route : routes) {
                    Map<String, String> params = new HashMap<>();
                    if (route.method.equals(method) && matchPath(route.pattern, path, params)) {
                        matchedRoute = route;
                        pathParams = params;
                        break;
                    }
                }

                // Jika tidak ada route yang cocok
                if (matchedRoute == null) {
                    // Cek apakah path-nya ada tapi method-nya salah
                    boolean pathExists = false;
                    for (Route route : routes) {
                        if (matchPath(route.pattern, path, new HashMap<>())) {
                            pathExists = true;
                            break;
                        }
                    }

                    if (pathExists) {
                        res.sendError(405, "Method " + method + " tidak diizinkan untuk " + path);
                    } else {
                        res.sendError(404, "Route tidak ditemukan: " + path);
                    }
                    return;
                }

                // Jalankan handler yang cocok
                req.setPathParams(pathParams);
                matchedRoute.handler.handle(req, res);

                // Jika handler tidak mengirim response apapun, kirim 200 default
                if (!res.isSent()) {
                    res.sendSuccess(null);
                }

            } catch (Exception e) {
                // Tangkap semua exception yang tidak tertangani di handler
                System.err.println("Error saat memproses request: " + e.getMessage());
                e.printStackTrace();
                if (!res.isSent()) {
                    res.sendError(500, "Internal Server Error: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Mencocokkan path request dengan pattern route.
     * Mendukung path parameter dalam kurung kurawal.
     * 
     * Contoh:
     *   Pattern: "/api/events/{id}"
     *   Path:    "/api/events/EVT-005"
     *   → cocok, params = {"id": "EVT-005"}
     * 
     *   Pattern: "/api/tiket/{id}/refund"
     *   Path:    "/api/tiket/TKT-042/refund"
     *   → cocok, params = {"id": "TKT-042"}
     * 
     * @param pattern Route pattern (bisa mengandung {param})
     * @param path    Path dari request yang masuk
     * @param params  Map yang akan diisi dengan path parameter yang diekstrak
     * @return true jika cocok, false jika tidak
     */
    private boolean matchPath(String pattern, String path, Map<String, String> params) {
        String[] patternParts = pattern.split("/");
        String[] pathParts = path.split("/");

        if (patternParts.length != pathParts.length) {
            return false;
        }

        for (int i = 0; i < patternParts.length; i++) {
            String segment = patternParts[i];
            if (segment.startsWith("{") && segment.endsWith("}")) {
                // Segment ini adalah path parameter — ekstrak nilainya
                String paramName = segment.substring(1, segment.length() - 1);
                params.put(paramName, pathParts[i]);
            } else if (!segment.equals(pathParts[i])) {
                // Segment tidak cocok
                return false;
            }
        }
        return true;
    }

    /**
     * Representasi internal untuk satu route yang terdaftar.
     */
    private static class Route {
        final String method;
        final String pattern;
        final RouteHandler handler;

        Route(String method, String pattern, RouteHandler handler) {
            this.method = method;
            this.pattern = pattern;
            this.handler = handler;
        }
    }
}
