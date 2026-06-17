package repository;

import java.util.UUID;

public final class RepoUtil {

    private RepoUtil() {
    }

    public static String generateId(String prefix) {
        return prefix + "-" + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }
}