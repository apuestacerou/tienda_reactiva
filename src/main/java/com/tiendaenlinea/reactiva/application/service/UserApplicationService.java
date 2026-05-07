package com.tiendaenlinea.reactiva.application.service;

import java.util.UUID;
import org.springframework.stereotype.Service;
import com.tiendaenlinea.reactiva.infrastructure.persistence.UserEntity;
import com.tiendaenlinea.reactiva.infrastructure.persistence.UserR2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class UserApplicationService{
    private final UserR2dbcRepository userRepository;

    public UserApplicationService(UserR2dbcRepository userRepository){
        this.userRepository=userRepository;
    }
    public Flux<UserEntity>ListarTodos(){
        return userRepository.findAll();
    }
    public Mono<UserEntity>obtenerPorId(UUID id){
        return userRepository.findById(id);
    }
    public Flux<UserEntity>buscarPorNombre(String name){
        return userRepository.findByFullNameContainingIgnoreCase(name);
    }
    public Mono <UserEntity>actualizar(UUID id, UserEntity updatedUser){
        return userRepository.findById(id).flatMap(user ->{
            user.setFullName(updatedUser.getFullName());
            user.setEmail(updatedUser.getEmail());
            return userRepository.save(user);
        });
    }
}