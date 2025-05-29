package com.estapar.parking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.estapar.parking.model.GarageSector;

@Repository
public interface GarageSectorRepository extends JpaRepository<GarageSector, String> {
} 