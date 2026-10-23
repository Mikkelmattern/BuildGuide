package mikkelmattern.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;
import java.util.Set;

@Entity
@Table(
        name = "reveals",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "node_id"})
)
@Getter
@NoArgsConstructor
public class Reveal {
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "node_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Node node;

    @Column(nullable = false, updatable = false)
    private Instant revealedAt;


    public Reveal(User user, Node node) {
        this.user = user;
        this.node = node;
    }

    @PrePersist
    void onCreate() {
        revealedAt = Instant.now();
    }

}
