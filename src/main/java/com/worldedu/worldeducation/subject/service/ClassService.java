package com.worldedu.worldeducation.subject.service;

import com.worldedu.worldeducation.subject.dto.ClassDTO;
import com.worldedu.worldeducation.subject.dto.ClassListResponse;
import com.worldedu.worldeducation.subject.entity.EdClass;
import com.worldedu.worldeducation.subject.repository.EdClassRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClassService {

    private final EdClassRepository edClassRepository;

    /**
     * Get all active classes
     * @return ClassListResponse containing all active classes
     */
    public ClassListResponse getAllClasses() {
        log.info("Fetching all active classes");

        List<EdClass> allClasses = edClassRepository.findByIsActiveTrue();

        List<ClassDTO> classDTOs = allClasses.stream()
                .map(edClass -> ClassDTO.builder()
                        .classId(edClass.getClassId())
                        .className(edClass.getClassName())
                        .classNumber(edClass.getClassNumber())
                        .isActive(edClass.getIsActive())
                        .build())
                .collect(Collectors.toList());

        log.info("Found {} active classes", classDTOs.size());

        return ClassListResponse.builder()
                .classes(classDTOs)
                .totalClasses(classDTOs.size())
                .build();
    }
}
