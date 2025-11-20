package com.elasticbeanstalk.mini_elastic_beanstalk.controller;

import com.elasticbeanstalk.mini_elastic_beanstalk.service.deploy.DeployService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("")
public class DeployController {

    @Autowired
    private DeployService deployService;

//    POST   /api/servers/{id}/deploy              - Deploy compose
//    GET    /api/servers/{id}/deploys             - Listar deploys
//    POST   /api/servers/{id}/deploy/upload       - Upload compose + env
//    DELETE /api/servers/{id}/deploy/{deployId}  - Remover deploy
}
