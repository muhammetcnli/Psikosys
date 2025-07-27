package com.atlas.Psikosys.repository;

import com.atlas.Psikosys.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ChatRepository extends JpaRepository<Chat, UUID> {


}
