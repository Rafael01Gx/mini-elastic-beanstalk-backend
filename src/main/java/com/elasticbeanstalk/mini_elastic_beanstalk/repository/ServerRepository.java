package com.elasticbeanstalk.mini_elastic_beanstalk.repository;

import com.elasticbeanstalk.mini_elastic_beanstalk.domain.entity.Server;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServerRepository extends JpaRepository<Server,String> {
    List<Server> findByUserId(Long userId);
}
