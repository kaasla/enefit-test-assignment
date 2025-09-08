package com.kaarelkaasla.enefitresourceservice.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kaarelkaasla.enefitresourceservice.entities.Resource;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {

  @Query(
      "SELECT r FROM Resource r LEFT JOIN FETCH r.location LEFT JOIN FETCH r.characteristics WHERE"
          + " r.id = :id")
  Optional<Resource> findByIdWithDetails(@Param("id") Long id);

  @Query("SELECT r FROM Resource r LEFT JOIN FETCH r.location LEFT JOIN FETCH r.characteristics")
  List<Resource> findAllWithDetails();
}
