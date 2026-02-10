package com.worldedu.worldeducation.subject.repository;

import com.worldedu.worldeducation.subject.entity.EdSubject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EdSubjectRepository extends JpaRepository<EdSubject, Long> {
    
    List<EdSubject> findByClassIdAndIsActiveTrue(Long classId);
    
    List<EdSubject> findByClassId(Long classId);
}
