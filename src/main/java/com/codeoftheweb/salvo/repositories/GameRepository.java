package com.codeoftheweb.salvo.repositories;

import com.codeoftheweb.salvo.entities.Game;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.jpa.repository.JpaRepository;

@RepositoryRestResource
public interface GameRepository extends JpaRepository<Game, Long> {

}
