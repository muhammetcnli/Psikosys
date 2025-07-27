package com.atlas.Psikosys.dto;

import com.atlas.Psikosys.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Oauth2UserInfoDto {

    private UUID id;

    private String firstName;

    private String lastName;

    private String email;

    private String picture;

    private String provider;

    private String providerId;

    private Integer messageLimit;

    private Integer limitResetDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Role role; // Eğer istersen burada sadece Role adı (String) da olabilir
}
