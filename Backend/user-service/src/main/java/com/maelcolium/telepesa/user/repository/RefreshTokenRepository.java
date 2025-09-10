package com.maelcolium.telepesa.user.repository;

import com.maelcolium.telepesa.user.model.RefreshToken;
import com.maelcolium.telepesa.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Query("update RefreshToken r set r.revoked = true where r.user = :user")
    void revokeAllByUser(@Param("user") User user);

    @Modifying
    @Query("delete from RefreshToken r where r.expiresAt < :now")
    void deleteAllExpired(@Param("now") LocalDateTime now);
}


