package com.audin3xt.backend.application.dtos;

public record LoginResponse(String token, String email, String role) {}