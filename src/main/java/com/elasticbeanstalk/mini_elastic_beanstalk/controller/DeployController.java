package com.elasticbeanstalk.mini_elastic_beanstalk.controller;

import com.elasticbeanstalk.mini_elastic_beanstalk.domain.dto.request.DeployRequest;
import com.elasticbeanstalk.mini_elastic_beanstalk.domain.dto.response.DeployResponse;
import com.elasticbeanstalk.mini_elastic_beanstalk.service.deploy.DeployService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("servers")
public class DeployController {

    @Autowired
    private DeployService deployService;

    @PostMapping("/{id}/deploy")
    public ResponseEntity<DeployResponse> deploy(@PathVariable("id") String id, @Valid DeployRequest dto, HttpServletRequest req) {
     return  ResponseEntity.ok().body(deployService.deploy(id, dto, req));
    }


    @GetMapping("/{id}/deploy")
    public ResponseEntity<List<DeployResponse>> listDeploys(@PathVariable("id") String id, HttpServletRequest req) {
        return ResponseEntity.ok().body(deployService.listDeploys(id, req));
    }

    @PostMapping("/{id}/deploy/upload")
    public void uploadCompose(@PathVariable("id") String id, @RequestParam("file") MultipartFile file) {

    }

    @DeleteMapping("/{id}/deploy/{deployId}")
    public ResponseEntity<String> deleteDeploy(@PathVariable("id") String id, @PathVariable("deployId") Long deployId,HttpServletRequest req) {
        deployService.removeDeploy(id,deployId,req);
        return ResponseEntity.ok().body("success");
    }
}
