package com.elasticbeanstalk.mini_elastic_beanstalk.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("servers")
public class ServerController {

//    GET    /api/servers           - Listar servidores do usuário
//    POST   /api/servers           - Criar novo servidor
//    GET    /api/servers/{id}      - Detalhes do servidor
//    PUT    /api/servers/{id}      - Atualizar servidor
//    DELETE /api/servers/{id}      - Remover servidor completo
    @GetMapping
    public ResponseEntity<String> list() {
       return  ResponseEntity.ok().body("Server List");
    }

    @PostMapping
    public ResponseEntity<String> createServer() {
        return  ResponseEntity.ok().body("Create Server");
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> details(@PathVariable String id) {
        return  ResponseEntity.ok().body("Get Server:" + id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateServer(@PathVariable String id) {
        return  ResponseEntity.ok().body("Update Server:" + id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteServer(@PathVariable String id) {
        return  ResponseEntity.ok().body("Delete Server:" + id);
    }
}
