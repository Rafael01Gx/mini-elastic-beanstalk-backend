package com.elasticbeanstalk.mini_elastic_beanstalk.domain.entity;

import com.elasticbeanstalk.mini_elastic_beanstalk.domain.enums.RuleDirection;
import io.hypersistence.utils.hibernate.type.array.ListArrayType;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "security_rules")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@Builder
public class SecurityRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    private Server server;

    @Enumerated(EnumType.STRING)
    private RuleDirection direction;

    private String protocol;
    private Integer portFrom;
    private Integer portTo;

    @Column(columnDefinition = "TEXT[]")
    private List<String> allowedIps;

    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

}