package com.elasticbeanstalk.mini_elastic_beanstalk.repository;

import com.elasticbeanstalk.mini_elastic_beanstalk.domain.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog,Long> {
}
