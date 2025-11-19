package com.elasticbeanstalk.mini_elastic_beanstalk.repository;

import com.elasticbeanstalk.mini_elastic_beanstalk.domain.entity.Deploy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeployRepository extends JpaRepository<Deploy, Long> {
    List<Deploy> findByServerId(String serverId);
}
