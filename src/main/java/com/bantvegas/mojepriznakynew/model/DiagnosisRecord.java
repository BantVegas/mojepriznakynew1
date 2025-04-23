package com.bantvegas.mojepriznakynew.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "diagnosis_record")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiagnosisRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String prompt;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String result;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;
}
