package com.elasticbeanstalk.mini_elastic_beanstalk.repository;

import com.elasticbeanstalk.mini_elastic_beanstalk.domain.entity.SecurityRule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SecurityRuleRepository extends JpaRepository<SecurityRule, Long> {
}
