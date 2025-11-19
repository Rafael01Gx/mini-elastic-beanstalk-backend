package com.elasticbeanstalk.mini_elastic_beanstalk.domain.entity;

import com.elasticbeanstalk.mini_elastic_beanstalk.domain.enums.ContainerStatus;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "containers")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@Builder
public class Container {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Server server;

    @ManyToOne(fetch = FetchType.LAZY)
    private Deploy deploy;

    private String name;
    private String image ;

    @Enumerated(EnumType.STRING)
    private ContainerStatus status ;

    @Type(JsonType.class)
    @Column(name = "ports", columnDefinition = "jsonb")
    private List<Integer> ports ;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}