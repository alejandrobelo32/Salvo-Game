package com.codeoftheweb.salvo.repositories;

import com.codeoftheweb.salvo.entities.GamePlayer;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.jpa.repository.JpaRepository;

@RepositoryRestResource
public interface GamePlayerRepository extends JpaRepository<GamePlayer, Long> {



}