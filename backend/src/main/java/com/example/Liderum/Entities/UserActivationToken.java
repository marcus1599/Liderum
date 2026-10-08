package com.example.Liderum.Entities;
import jakarta.persistence.*;
import lombok.Getter; import lombok.NoArgsConstructor; import lombok.Setter;
import java.time.Instant;
@Entity @Table(name="user_activation_tokens", indexes={@Index(name="idx_activation_token_hash",columnList="token_hash",unique=true),@Index(name="idx_activation_token_user",columnList="user_id")})
@Getter @Setter @NoArgsConstructor
public class UserActivationToken {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(optional=false,fetch=FetchType.LAZY) @JoinColumn(name="user_id",nullable=false) private User user;
 @Column(name="token_hash",nullable=false,unique=true,length=64) private String tokenHash;
 @Column(name="expires_at",nullable=false) private Instant expiresAt;
 @Column(name="used_at") private Instant usedAt;
 @Column(name="revoked_at") private Instant revokedAt;
}
