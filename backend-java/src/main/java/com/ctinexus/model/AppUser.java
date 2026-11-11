package com.ctinexus.model;

public record AppUser(
        long id,
        String name,
        String email,
        String passwordHash,
        String role,
        String status,
        String lastAccess,
        int tokenVersion
) {
}
