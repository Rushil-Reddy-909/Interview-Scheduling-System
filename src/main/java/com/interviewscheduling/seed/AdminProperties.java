package com.interviewscheduling.seed;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

@ConfigurationProperties(prefix = "app.admin")
@Validated
public record AdminProperties(@NotBlank String password) {
}
