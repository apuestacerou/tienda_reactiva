package com.tiendaenlinea.reactiva.infrastructure.web;

import java.util.UUID;
import org.springframework.web.bind.annotation.*;
import com.tiendaenlinea.reactiva.application.service.UserApplicationService;
import com.tiendaenlinea.reactiva.infrastructure.persistence.UserEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserApplicationService userService;
    
    public UserController(UserApplicationService userService){
        this.userService=userService;
    }
    @GetMapping
    public Flux<UserEntity>Listar(){
        return userService.ListarTodos();
    }
    @GetMapping("/{id}")
    public Mono<UserEntity>obtener(@PathVariable UUID id){
        return userService.obtenerPorId(id);
    }
    @GetMapping("/search")
    public Flux<UserEntity>buscar(@RequestParam String name){
        return userService.buscarPorNombre(name);
    }
    @PutMapping("/{id}")
    public Mono<UserEntity>actualizar(@PathVariable UUID id, @RequestBody UserEntity user){
        return userService.actualizar(id, user);
    }
}