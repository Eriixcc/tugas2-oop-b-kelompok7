package service;

import model.User;
import repository.UserRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class UserService {

    private final UserRepository userRepository;

    public UserService() {
        this.userRepository = new UserRepository();
    }

    public List<User> getUsers(String role) throws SQLException {
        return userRepository.findAll(role);
    }

    public User getUser(String id) throws SQLException {

        User user = userRepository.findById(id);

        if (user == null) {
            throw new IllegalArgumentException("User tidak ditemukan");
        }

        return user;
    }

    public User createUser(User user) throws SQLException {

        if (user.getName() == null || user.getName().isBlank()) {
            throw new IllegalArgumentException("Nama wajib diisi");
        }

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email wajib diisi");
        }

        if (user.getRole() == null || user.getRole().isBlank()) {
            throw new IllegalArgumentException("Role wajib diisi");
        }

        return userRepository.create(user);
    }

    public User updateUser(String id, User user) throws SQLException {

        if (userRepository.findById(id) == null) {
            throw new IllegalArgumentException("User tidak ditemukan");
        }

        return userRepository.update(id, user);
    }

    public Map<String, Object> getBuyerSummary(String id)
            throws SQLException {

        return userRepository.buyerSummary(id);
    }

    public Map<String, Object> getOrganizerSummary(String id)
            throws SQLException {

        return userRepository.organizerSummary(id);
    }
}