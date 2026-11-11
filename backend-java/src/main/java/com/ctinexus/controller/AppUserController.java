package com.ctinexus.controller;

import com.ctinexus.dto.AdminUserRequest;
import com.ctinexus.service.AppUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class AppUserController {

    private final AppUserService appUserService;

    public AppUserController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @GetMapping
    public List<Map<String, Object>> listUsers() {
        return appUserService.listUsers();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> createUser(@Valid @RequestBody AdminUserRequest request) {
        return appUserService.publicUser(appUserService.createByAdmin(request));
    }

    @PutMapping("/{id}")
    public Map<String, Object> updateUser(
            @PathVariable long id,
            @Valid @RequestBody AdminUserRequest request
    ) {
        return appUserService.publicUser(appUserService.updateByAdmin(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable long id) {
        appUserService.delete(id);
    }
}
