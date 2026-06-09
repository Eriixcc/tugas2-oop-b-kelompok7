package server;

/**
 * Interface untuk menangani HTTP request pada suatu route.
 * 
 * Contoh penggunaan dengan lambda:
 *   server.get("/api/events", (req, res) -> {
 *       res.sendSuccess(data);
 *   });
 * 
 * Atau dengan method reference:
 *   server.get("/api/events", EventHandler::getAll);
 */
public interface RouteHandler {
    void handle(Request req, Response res) throws Exception;
}
