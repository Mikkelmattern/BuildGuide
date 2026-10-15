package mikkelmattern.utils;

import jakarta.persistence.EntityManagerFactory;
import mikkelmattern.entities.User;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Fills the database with users from a text file in {@code src/main/resources}.
 */
public class Populator {

    /**
     * Reads the file and creates users from it. Not finished yet: the users are
     * built but not filled in or saved.
     *
     * @param path the file's path inside {@code src/main/resources}
     * @param emf  the entity manager factory to save the users with
     * @throws IOException if the file can't be read
     */
    public static void populate(String path, EntityManagerFactory emf) throws IOException {
        String content = readResource(path);

        // Split the file into numbered sections ("1. ", "2. ", ...), skipping the text before the first one
        String[] sections = content.split("\\R(?=\\d+\\. )");
        for (String section : Arrays.copyOfRange(sections, 1, sections.length)) {
            // Split each section into at most 3 lines
            String[] parts = section.strip().split("\\R", 3);
            User user = User.builder()
                    .build();
        }
    }

    /**
     * Reads a file from the classpath as a UTF-8 string.
     *
     * @param path the file's path inside {@code src/main/resources}
     * @return the file's contents
     * @throws IllegalStateException if the file doesn't exist
     * @throws UncheckedIOException  if the file can't be read
     */
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
