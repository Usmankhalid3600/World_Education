package com.worldedu.worldeducation.subject.repository;

import com.worldedu.worldeducation.subject.entity.EdClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EdClassRepository extends JpaRepository<EdClass, Long> {
    
    List<EdClass> findByIsActiveTrue();
}
