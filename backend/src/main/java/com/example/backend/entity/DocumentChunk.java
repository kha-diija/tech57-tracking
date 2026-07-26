package com.example.backend.entity;

import com.example.backend.converter.VectorConverter;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "document_chunk")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class DocumentChunk {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_chunk")
    private Long idChunk;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_source", nullable = false)
    private DocumentSource documentSource;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenu;

    @Convert(converter = VectorConverter.class)
    @Column(nullable = false, columnDefinition = "vector(768)")
    private float[] embedding;

    @Column(columnDefinition = "jsonb")
    private String metadata;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();
}