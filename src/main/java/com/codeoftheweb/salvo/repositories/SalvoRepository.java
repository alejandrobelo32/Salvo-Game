package com.codeoftheweb.salvo.repositories;

import com.codeoftheweb.salvo.entities.Salvo;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.jpa.repository.JpaRepository;

@RepositoryRestResource
public interface SalvoRepository extends JpaRepository<Salvo, Long> {

}

