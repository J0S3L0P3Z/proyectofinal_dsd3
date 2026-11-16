package co.edu.unbosque.webscraper.repository;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.webscraper.model.TokenType;
import co.edu.unbosque.webscraper.model.UserToken;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

/**
 * Repositorio de los tokens de un solo uso (verificación de correo y recuperación de contraseña).
 */
public interface UserTokenRepository extends JpaRepository<UserToken, Long> {
    Optional<UserToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("update UserToken t set t.usedAt = :now where t.userId = :userId and t.type = :type and t.usedAt is null")
    int invalidateOpen(@Param("userId") Long userId, @Param("type") TokenType type, @Param("now") Instant now);

    @Modifying
    @Query("delete from UserToken t where t.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
