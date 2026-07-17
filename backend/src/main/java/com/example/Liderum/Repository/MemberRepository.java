package com.example.Liderum.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Liderum.Entities.Member;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    List<Member> findAllByGuildId(Long guildId);

    Optional<Member> findByIdAndGuildId(Long id, Long guildId);
}
