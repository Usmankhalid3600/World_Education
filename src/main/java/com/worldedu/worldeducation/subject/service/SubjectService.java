package com.worldedu.worldeducation.subject.service;

import com.worldedu.worldeducation.subject.dto.SubjectDTO;
import com.worldedu.worldeducation.subject.dto.SubjectListResponse;
import com.worldedu.worldeducation.subject.entity.EdClass;
import com.worldedu.worldeducation.subject.entity.EdSubject;
import com.worldedu.worldeducation.subject.entity.UserSubjectSubscription;
import com.worldedu.worldeducation.subject.repository.EdClassRepository;
import com.worldedu.worldeducation.subject.repository.EdSubjectRepository;
import com.worldedu.worldeducation.subject.repository.UserSubjectSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubjectService {

    private final EdSubjectRepository edSubjectRepository;
    private final UserSubjectSubscriptionRepository userSubjectSubscriptionRepository;
    private final EdClassRepository edClassRepository;

    /**
     * Get opted and unopted subjects for a class
     * @param classId The class ID
     * @param customerId The logged-in user's customer ID
     * @return SubjectListResponse with opted and unopted subjects
     */
    public SubjectListResponse getSubjectsByClass(Long classId, Long customerId) {
        log.info("Fetching subjects for classId: {} and customerId: {}", classId, customerId);

        // Get class information
        EdClass edClass = edClassRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found with id: " + classId));

        // Get all active subjects for the class
        List<EdSubject> allSubjects = edSubjectRepository.findByClassIdAndIsActiveTrue(classId);

        // Get user's subscribed subjects
        List<UserSubjectSubscription> userSubscriptions = 
            userSubjectSubscriptionRepository.findByCustomerIdAndIsActiveTrue(customerId);
        
        Set<Long> subscribedSubjectIds = userSubscriptions.stream()
                .map(UserSubjectSubscription::getSubjectId)
                .collect(Collectors.toSet());

        // Separate opted and unopted subjects
        List<SubjectDTO> optedSubjects = new ArrayList<>();
        List<SubjectDTO> unoptedSubjects = new ArrayList<>();

        for (EdSubject subject : allSubjects) {
            boolean isOpted = subscribedSubjectIds.contains(subject.getSubjectId());
            
            SubjectDTO subjectDTO = SubjectDTO.builder()
                    .subjectId(subject.getSubjectId())
                    .classId(subject.getClassId())
                    .subjectName(subject.getSubjectName())
                    .isActive(subject.getIsActive())
                    .isOpted(isOpted)
                    .build();

            if (isOpted) {
                optedSubjects.add(subjectDTO);
            } else {
                unoptedSubjects.add(subjectDTO);
            }
        }

        log.info("Found {} opted and {} unopted subjects for classId: {}", 
                optedSubjects.size(), unoptedSubjects.size(), classId);

        return SubjectListResponse.builder()
                .classId(classId)
                .className(edClass.getClassName())
                .optedSubjects(optedSubjects)
                .unoptedSubjects(unoptedSubjects)
                .totalSubjects(allSubjects.size())
                .optedCount(optedSubjects.size())
                .unoptedCount(unoptedSubjects.size())
                .build();
    }
}
