package com.example.cp_main_be.domain.avatar.image.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ReplicatePredictionResponse(String id, String status, String output, String error) {}
