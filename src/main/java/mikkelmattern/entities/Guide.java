package mikkelmattern.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * A guide written by a user, stored in the {@code guides} table.
 * <p>
 * {@code createdAt} and {@code updatedAt} are set automatically when the guide is saved or changed.
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
@Table(name = "guides")
@ToString
public class Guide {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @OneToMany(mappedBy = "guide", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<Node> nodes = new ArrayList<>();   // all nodes in this guide

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant updatedAt;

    // The user who wrote the guide
    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    @ToString.Exclude
    private User author;

    // Users who have saved this guide. Owned by User.savedGuides
    @ManyToMany(mappedBy = "savedGuides", fetch = FetchType.LAZY)
    @ToString.Exclude
    @Builder.Default
    private List<User> savedBy = new java.util.ArrayList<>();

    // The game associated with the guide
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id")
    private Game game;

    /**
     * Sets {@code createdAt} and {@code updatedAt} to now. Called by JPA before the guide is first saved.
     */
    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    /**
     * Sets {@code updatedAt} to now. Called by JPA before the guide is updated.
     */
    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public void addNode(Node node){
        nodes.add(node);
        node.setGuide(this);
    }


}
