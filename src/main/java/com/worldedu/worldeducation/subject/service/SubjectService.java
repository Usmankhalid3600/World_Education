package com.worldedu.worldeducation.subject.service;

import com.worldedu.worldeducation.subject.dto.SubjectDTO;
import com.worldedu.worldeducation.subject.dto.SubjectListResponse;
import com.worldedu.worldeducation.subject.entity.EdClass;
import com.worldedu.worldeducation.subject.entity.EdSubject;
import com.worldedu.worldeducation.subject.entity.UserSubjectSubscription;
import com.worldedu.worldeducation.subject.repository.EdClassRepository;
import com.worldedu.worldeducation.subject.repository.EdSubjectRepository;
import com.worldedu.worldeducation.subject.repository.UserSubjectSubscriptionRepository;
import com.worldedu.worldeducation.enums.UserCategory;
import com.worldedu.worldeducation.subscription.repository.SubscriptionPlanRepository;
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
    private final SubscriptionPlanRepository subscriptionPlanRepository;

    /**
     * Get opted and unopted subjects for a class.
     * Unopted subjects are only shown if a subject-level subscription plan exists for them.
     * Subjects with no price set are hidden from students.
     * Already-subscribed subjects are always shown.
     */
    public SubjectListResponse getSubjectsByClass(Long classId, Long customerId, UserCategory userCategory) {
        log.info("Fetching subjects for classId: {} and customerId: {}", classId, customerId);

        // Get class information
        EdClass edClass = edClassRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found with id: " + classId));

        // ADMIN sees all subjects (including inactive); students see only active ones
        List<EdSubject> allSubjects = (userCategory == UserCategory.ADMIN)
                ? edSubjectRepository.findByClassId(classId)
                : edSubjectRepository.findByClassIdAndIsActiveTrue(classId);

        // Get user's active subscribed subjects
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

            // Check if user previously had an inactive subscription for this subject
            boolean subscriptionInactive = !isOpted && userCategory != UserCategory.ADMIN
                    && userSubjectSubscriptionRepository
                            .existsByCustomerIdAndSubjectIdAndIsActiveFalse(customerId, subject.getSubjectId());

            // For unopted subjects with no prior subscription: apply plan/price filter for students.
            // Subjects with inactive subscriptions are always shown (so student knows access was revoked).
            if (!isOpted && !subscriptionInactive && userCategory != UserCategory.ADMIN) {
                boolean subjectPlanExists = subscriptionPlanRepository
                        .findBySubjectIdAndIsActiveTrue(subject.getSubjectId()).isPresent();
                if (!subjectPlanExists) {
                    log.debug("Hiding subject {} — no subject-level subscription plan set", subject.getSubjectName());
                    continue;
                }
            }

            SubjectDTO subjectDTO = SubjectDTO.builder()
                    .subjectId(subject.getSubjectId())
                    .classId(subject.getClassId())
                    .subjectName(subject.getSubjectName())
                    .isActive(subject.getIsActive())
                    .isOpted(isOpted)
                    .subscriptionInactive(subscriptionInactive)
                    .description(subject.getDescription())
                    .build();

            if (isOpted) {
                optedSubjects.add(subjectDTO);
            } else {
                unoptedSubjects.add(subjectDTO);
            }
        }

        log.info("Found {} opted and {} unopted subjects (with plans) for classId: {}",
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
