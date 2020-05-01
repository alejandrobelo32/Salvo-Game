package com.codeoftheweb.salvo.repositories;

import com.codeoftheweb.salvo.entities.Ship;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.jpa.repository.JpaRepository;

@RepositoryRestResource
public interface ShipRepository extends JpaRepository<Ship, Long> {

}
