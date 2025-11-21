package com.elasticbeanstalk.mini_elastic_beanstalk.domain.entity;

import com.elasticbeanstalk.mini_elastic_beanstalk.domain.enums.DeployStatus;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "deploys")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@Builder
public class Deploy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonAlias("server_id")
    private Server server;

    private String workspace;
    private String composePath;
    private String envPath;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DeployStatus status = DeployStatus.PENDING;

    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}