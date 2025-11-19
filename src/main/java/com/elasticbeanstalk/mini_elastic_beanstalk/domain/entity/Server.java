package com.elasticbeanstalk.mini_elastic_beanstalk.domain.entity;

import com.elasticbeanstalk.mini_elastic_beanstalk.domain.enums.ServerStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "servers")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@Builder
public class Server {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    private String name;
    private String description;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ServerStatus status = ServerStatus.CREATED;

    private String networkId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}