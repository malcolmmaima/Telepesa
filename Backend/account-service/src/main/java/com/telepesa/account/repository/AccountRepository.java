package com.telepesa.account.repository;

import com.telepesa.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountNumber(String accountNumber);

    List<Account> findByUserId(Long userId);

    List<Account> findByStatus(String status);

    @Query("SELECT a FROM Account a WHERE a.userId = :userId AND a.status = :status")
    List<Account> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);
}
