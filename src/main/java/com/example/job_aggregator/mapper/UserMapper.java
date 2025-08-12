package com.example.job_aggregator.mapper;

import com.example.job_aggregator.model.User;
import com.example.job_aggregator.resource.UserResource;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResource toResource(User entity) {
        if (entity == null) {
            return null;
        }

        return UserResource.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .build();
        // Note: Never map password to resource
    }

    public User toEntity(UserResource resource) {
        if (resource == null) {
            return null;
        }

        User entity = new User();
        entity.setId(resource.getId());
        entity.setUsername(resource.getUsername());
        // Password would be handled separately with encryption
        return entity;
    }
}
