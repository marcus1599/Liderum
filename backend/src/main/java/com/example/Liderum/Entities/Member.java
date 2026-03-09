package com.example.Liderum.Entities;

import java.util.List;

import com.example.Liderum.Enums.Classe;
import com.example.Liderum.Enums.GuildRole;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nickname;

    private String phone;

    @Enumerated(EnumType.STRING)
    private GuildRole guildRole;

    private String rank; 


    @ManyToOne
    @JsonIgnore
    private Team team;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Attendance> attendances;

    
    @OneToOne(mappedBy = "member")
    @JsonIgnore
    private User user;
    
    @Enumerated(EnumType.STRING)
    private Classe classe;
}
