package com.worldedu.worldeducation.subscription.dto;

import com.worldedu.worldeducation.subscription.entity.SubscriptionPlan;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSubscriptionPlanRequest {
    @NotBlank(message = "Plan name is required")
    @Size(min = 3, max = 100, message = "Plan name must be between 3 and 100 characters")
    private String planName;
    
    @NotNull(message = "Target type is required")
    private SubscriptionPlan.TargetType targetType;
    
    @NotNull(message = "Target ID is required")
    @Positive(message = "Target ID must be positive")
    private Long targetId;
    
    @NotNull(message = "Duration days is required")
    @Positive(message = "Duration days must be positive")
    private Integer durationDays;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;
    
    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters (e.g., USD, PKR)")
    private String currency;
    
    @Min(value = 0, message = "Grace period days cannot be negative")
    private Integer gracePeriodDays;
    
    @Min(value = 0, message = "Free days cannot be negative")
    private Integer freeDays;
    
    private Boolean isActive = true;
}
