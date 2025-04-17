package com.example.Liderum.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Liderum.Entities.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
