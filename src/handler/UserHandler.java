package handler;

import model.User;
import server.Request;
import server.Response;
import service.UserService;

import java.util.Map;

public class UserHandler {

    private static final UserService userService =
            new UserService();

    public static void getUsers(Request req, Response res)
            throws Exception {

        String role = req.getQueryParam("role");

        res.sendSuccess(
                userService.getUsers(role)
        );
    }

    public static void getUser(Request req, Response res)
            throws Exception {

        String id = req.getPathParam("id");

        res.sendSuccess(
                userService.getUser(id)
        );
    }

    public static void createUser(Request req, Response res)
            throws Exception {

        Map<String, Object> body =
                req.getJSON();

        User user = new User();

        user.setName((String) body.get("name"));
        user.setEmail((String) body.get("email"));
        user.setPhone((String) body.get("phone"));
        user.setRole((String) body.get("role"));

        res.sendCreated(
                userService.createUser(user)
        );
    }

    public static void updateUser(Request req, Response res)
            throws Exception {

        String id =
                req.getPathParam("id");

        Map<String, Object> body =
                req.getJSON();

        User user = new User();

        user.setName((String) body.get("name"));
        user.setEmail((String) body.get("email"));
        user.setPhone((String) body.get("phone"));
        user.setRole((String) body.get("role"));

        res.sendSuccess(
                userService.updateUser(id, user)
        );
    }
}