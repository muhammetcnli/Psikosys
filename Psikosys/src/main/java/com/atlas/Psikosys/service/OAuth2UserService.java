package com.atlas.Psikosys.service;

import com.atlas.Psikosys.dto.Oauth2UserInfoDto;
import com.atlas.Psikosys.entity.Role;
import com.atlas.Psikosys.entity.User;
import com.atlas.Psikosys.repository.RoleRepository;
import com.atlas.Psikosys.repository.UserRepository;
import com.atlas.Psikosys.security.UserPrincipal;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public OAuth2UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);
        return processOAuth2User(oAuth2UserRequest, oAuth2User);
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        String fullName = oAuth2User.getAttributes().get("name").toString();
        String firstName = fullName;
        String lastName = "";

        if (fullName.contains(" ")) {
            int lastSpaceIndex = fullName.lastIndexOf(" ");
            firstName = fullName.substring(0, lastSpaceIndex);
            lastName = fullName.substring(lastSpaceIndex + 1);
        }

        Oauth2UserInfoDto userInfoDto = Oauth2UserInfoDto
                .builder()
                .firstName(firstName)
                .lastName(lastName)
                .providerId(oAuth2User.getAttributes().get("sub").toString()) // UUID yerine String olarak providerId
                .email(oAuth2User.getAttributes().get("email").toString())
                .picture(oAuth2User.getAttributes().get("picture").toString())
                .provider(oAuth2UserRequest.getClientRegistration().getRegistrationId())
                .build();

        Optional<User> userOptional = userRepository.findByEmail(userInfoDto.getEmail());

        User user = userOptional
                .map(existingUser -> updateExistingUser(existingUser, userInfoDto))
                .orElseGet(() -> registerNewUser(oAuth2UserRequest, userInfoDto));

        return UserPrincipal.create(user, oAuth2User.getAttributes());
    }

    private User registerNewUser(OAuth2UserRequest oAuth2UserRequest, Oauth2UserInfoDto userInfoDto) {
        // Default role'ü veritabanından al, yoksa oluştur
        Role defaultRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> createDefaultRole());

        User user = User.builder()
                .provider(userInfoDto.getProvider())
                .providerId(userInfoDto.getProviderId())
                .firstName(userInfoDto.getFirstName())
                .lastName(userInfoDto.getLastName())
                .email(userInfoDto.getEmail())
                .picture(userInfoDto.getPicture())
                .role(defaultRole)
                .messageLimit(10) // Default message limit
                .limitResetDate(1) // Default limit reset date
                .build();

        return userRepository.save(user);
    }

    private Role createDefaultRole() {
        Role defaultRole = Role.builder()
                .name("ROLE_USER")
                .build();
        return roleRepository.save(defaultRole);
    }

    private User updateExistingUser(User existingUser, Oauth2UserInfoDto userInfoDto) {
        existingUser.setFirstName(userInfoDto.getFirstName());
        existingUser.setLastName(userInfoDto.getLastName());
        existingUser.setPicture(userInfoDto.getPicture());

        // Provider bilgilerini güncelle (eğer boşsa)
        if (existingUser.getProvider() == null) {
            existingUser.setProvider(userInfoDto.getProvider());
        }
        if (existingUser.getProviderId() == null) {
            existingUser.setProviderId(userInfoDto.getProviderId());
        }

        return userRepository.save(existingUser);
    }
}