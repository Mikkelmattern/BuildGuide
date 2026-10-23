package mikkelmattern.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "nodes")
public class Node {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private int priorityNumber;
    private int storyOrderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guide_id", nullable = false)
    private Guide guide;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Node parent;                       // null = top-level node

    @OneToMany(mappedBy = "parent")
    private List<Node> children = new ArrayList<>();

    public void addChild(Node child){
        children.add(child);
        child.setParent(this);
        child.setGuide(this.guide);
    }
}
