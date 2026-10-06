package mikkelmattern.utils;

import jakarta.persistence.EntityManagerFactory;
import mikkelmattern.entities.User;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Populator {

    public static void populate(String path, EntityManagerFactory emf) throws IOException {
        String content = readResource(path);
        String[] sections = content.split("\\R(?=\\d+\\. )");
        for (String section : Arrays.copyOfRange(sections, 1, sections.length)) {
            String[] parts = section.strip().split("\\R", 3);
            User user = User.builder()
                    .build();
        }
    }

    private static String readResource(String path) throws IOException {
        try (InputStream in = Populator.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalStateException(path + " not found in src/main/resources");
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
