package com.example.job_aggregator.resource;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResource {
    private Long id;
    private String username;

    @JsonIgnore  //
    private String password;
}
