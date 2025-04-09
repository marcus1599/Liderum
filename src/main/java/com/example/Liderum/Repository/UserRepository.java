package com.example.Liderum.Repository;

import com.example.Liderum.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
