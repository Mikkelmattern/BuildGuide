package mikkelmattern.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "games")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long rawgId;            // so the same game can't be imported twice

    @Column(nullable = false)
    private String name;

    private String coverImageUrl;
    private LocalDate released;

}
