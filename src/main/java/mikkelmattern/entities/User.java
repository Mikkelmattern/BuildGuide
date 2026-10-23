package mikkelmattern.entities;

import jakarta.persistence.*;
import lombok.*;
import mikkelmattern.security.ISecurityUser;
import mikkelmattern.enums.Role;
import org.mindrot.jbcrypt.BCrypt;


import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A user of the application, stored in the {@code users} table.
 * <p>
 * Passwords are stored as BCrypt hashes, never as plain text. Roles are stored
 * in the {@code user_roles} table.
 */
@NoArgsConstructor
@Setter
@Getter
@ToString
@Table(name = "users")
@Entity
public class User implements ISecurityUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;


    // No Lombok getter or setter, so the hash can only be set through setPassword
    @Column(nullable = false)
    @ToString.Exclude
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private String hashedPassword;

    // Loaded eagerly, since roles are needed every time a user logs in
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    @ToString.Exclude
    private Set<Role> roles = new HashSet<>();

    // Guides this user has written
    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<Guide> authoredGuides = new HashSet<>();

    // Guides this user has saved, stored in the saved_guides join table
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "saved_guides",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "guide_id")
    )
    @ToString.Exclude
    private Set<Guide> savedGuides = new HashSet<>();

    /**
     * Creates a user and hashes their password. Used by {@code User.builder()}.
     *
     * @param username       the username
     * @param password       the plain-text password, hashed before it is stored, or {@code null} for none
     * @param email          the email
     * @param authoredGuides the guides the user has written, or {@code null} for none
     * @param savedGuides    the guides the user has saved, or {@code null} for none
     */
    @Builder
    public User(String username, String password, String email, Set<Guide> authoredGuides, Set<Guide> savedGuides) {
        this.username = username;
        this.email = email;
        if (authoredGuides != null) this.authoredGuides = authoredGuides;
        if (savedGuides != null) this.savedGuides = savedGuides;
        // No password when built from a UserOutDTO, so there is nothing to hash
        if (password != null) this.hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
    }

    /**
     * Hashes and stores a new password.
     *
     * @param password the new plain-text password
     */
    public void setPassword(String password) {
        hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
    }

    @Override
    public Set<String> getRolesAsStrings() {
        return roles.stream().map(Role::name).collect(Collectors.toSet());
    }

    @Override
    public boolean verifyPassword(String pw) {
        if (pw == null) {
            return false;
        }
        return BCrypt.checkpw(pw, hashedPassword);
    }

    @Override
    public void addRole(Role role) {

        roles.add(role);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if {@code role} isn't the name of a {@link Role}
     */
    @Override
    public void removeRole(String role) {
        roles.remove(Role.valueOf(role));
    }
}
