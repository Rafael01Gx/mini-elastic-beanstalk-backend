package com.elasticbeanstalk.mini_elastic_beanstalk.controller;

import com.elasticbeanstalk.mini_elastic_beanstalk.domain.dto.request.CreateServerRequest;
import com.elasticbeanstalk.mini_elastic_beanstalk.domain.dto.request.UpdateServerRequest;
import com.elasticbeanstalk.mini_elastic_beanstalk.domain.dto.response.ServerResponse;
import com.elasticbeanstalk.mini_elastic_beanstalk.service.server.ServerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("servers")
public class ServerController {
    @Autowired
    private ServerService serverService;


//    DELETE /api/servers/{id}      - Remover servidor completo
    @GetMapping
    public ResponseEntity<List<ServerResponse>> list(HttpServletRequest request) {
        return  ResponseEntity.ok().body(serverService.listUserServers(request));
    }

    @PostMapping
    public ResponseEntity<ServerResponse> createServer(@RequestBody CreateServerRequest dto, HttpServletRequest request) {
        return ResponseEntity.ok().body(serverService.createServer(dto,request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServerResponse> details(@PathVariable String id,HttpServletRequest request) {
        return  ResponseEntity.ok().body(serverService.getServer(id,request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServerResponse> updateServer(@PathVariable String id, @Valid UpdateServerRequest dto, HttpServletRequest request) {
        return  ResponseEntity.ok().body(serverService.updateServer(id,dto,request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteServer(@PathVariable String id) {
        return  ResponseEntity.ok().body("Delete Server:" + id);
    }
}
