package com.example.Liderum.dto;
import lombok.AllArgsConstructor; import lombok.Getter;
import com.example.Liderum.Enums.GuildRole; import com.example.Liderum.Enums.UserStatus;
@Getter @AllArgsConstructor public class UserActivationResponseDTO {
 private UserResponseDTO user; private String activationToken;
 public Long getId(){return user.getId();} public String getUsername(){return user.getUsername();} public String getEmail(){return user.getEmail();}
 public GuildRole getGuildRole(){return user.getGuildRole();} public UserStatus getStatus(){return user.getStatus();}
}
