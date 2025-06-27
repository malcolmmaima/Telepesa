package com.maelcolium.telepesa.account.repository;

import com.maelcolium.telepesa.account.model.Account;
import com.maelcolium.telepesa.models.enums.AccountStatus;
import com.maelcolium.telepesa.models.enums.AccountType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Account entity operations.
 * 
 * @author Telepesa Development Team
 * @version 1.0.0
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    /**
     * Find account by account number
     */
    Optional<Account> findByAccountNumber(String accountNumber);

    /**
     * Check if account number exists
     */
    boolean existsByAccountNumber(String accountNumber);

    /**
     * Find all accounts by user ID
     */
    List<Account> findByUserId(Long userId);

    /**
     * Find all accounts by user ID with pagination
     */
    Page<Account> findByUserId(Long userId, Pageable pageable);

    /**
     * Find accounts by user ID and status
     */
    List<Account> findByUserIdAndStatus(Long userId, AccountStatus status);

    /**
     * Find accounts by user ID and account type
     */
    List<Account> findByUserIdAndAccountType(Long userId, AccountType accountType);

    /**
     * Find accounts by status
     */
    Page<Account> findByStatus(AccountStatus status, Pageable pageable);

    /**
     * Find accounts by account type
     */
    Page<Account> findByAccountType(AccountType accountType, Pageable pageable);

    /**
     * Find active accounts by user ID
     */
    @Query("SELECT a FROM Account a WHERE a.userId = :userId AND a.status = 'ACTIVE' AND a.isFrozen = false")
    List<Account> findActiveAccountsByUserId(@Param("userId") Long userId);

    /**
     * Find accounts with balance greater than specified amount
     */
    @Query("SELECT a FROM Account a WHERE a.balance > :amount")
    Page<Account> findAccountsWithBalanceGreaterThan(@Param("amount") BigDecimal amount, Pageable pageable);

    /**
     * Find accounts with balance less than minimum balance
     */
    @Query("SELECT a FROM Account a WHERE a.balance < a.minimumBalance")
    List<Account> findAccountsBelowMinimumBalance();

    /**
     * Find dormant accounts (no transactions within specified period)
     */
    @Query("SELECT a FROM Account a WHERE a.lastTransactionDate < :cutoffDate OR a.lastTransactionDate IS NULL")
    List<Account> findDormantAccounts(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Count accounts by user ID
     */
    long countByUserId(Long userId);

    /**
     * Count accounts by status
     */
    long countByStatus(AccountStatus status);

    /**
     * Count accounts by account type
     */
    long countByAccountType(AccountType accountType);

    /**
     * Calculate total balance for user across all accounts
     */
    @Query("SELECT COALESCE(SUM(a.balance), 0) FROM Account a WHERE a.userId = :userId AND a.status = 'ACTIVE'")
    BigDecimal calculateTotalBalanceForUser(@Param("userId") Long userId);

    /**
     * Calculate total balance for account type
     */
    @Query("SELECT COALESCE(SUM(a.balance), 0) FROM Account a WHERE a.accountType = :accountType AND a.status = 'ACTIVE'")
    BigDecimal calculateTotalBalanceForAccountType(@Param("accountType") AccountType accountType);

    /**
     * Find accounts created within date range
     */
    @Query("SELECT a FROM Account a WHERE a.createdAt BETWEEN :startDate AND :endDate")
    Page<Account> findAccountsCreatedBetween(@Param("startDate") LocalDateTime startDate, 
                                           @Param("endDate") LocalDateTime endDate, 
                                           Pageable pageable);

    /**
     * Update account status
     */
    @Modifying
    @Query("UPDATE Account a SET a.status = :status, a.updatedAt = :updatedAt WHERE a.id = :accountId")
    int updateAccountStatus(@Param("accountId") Long accountId, 
                           @Param("status") AccountStatus status, 
                           @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * Update account balance
     */
    @Modifying
    @Query("UPDATE Account a SET a.balance = :balance, a.availableBalance = :availableBalance, " +
           "a.lastTransactionDate = :transactionDate, a.updatedAt = :updatedAt WHERE a.id = :accountId")
    int updateAccountBalance(@Param("accountId") Long accountId, 
                            @Param("balance") BigDecimal balance,
                            @Param("availableBalance") BigDecimal availableBalance,
                            @Param("transactionDate") LocalDateTime transactionDate,
                            @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * Freeze/unfreeze account
     */
    @Modifying
    @Query("UPDATE Account a SET a.isFrozen = :frozen, a.updatedAt = :updatedAt WHERE a.id = :accountId")
    int updateAccountFreezeStatus(@Param("accountId") Long accountId, 
                                 @Param("frozen") Boolean frozen, 
                                 @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * Find accounts by currency code
     */
    List<Account> findByCurrencyCode(String currencyCode);

    /**
     * Find accounts with overdraft facility
     */
    @Query("SELECT a FROM Account a WHERE a.overdraftAllowed = true")
    List<Account> findAccountsWithOverdraft();

    /**
     * Find accounts by user ID and currency
     */
    List<Account> findByUserIdAndCurrencyCode(Long userId, String currencyCode);

    /**
     * Search accounts by account name or description
     */
    @Query("SELECT a FROM Account a WHERE " +
           "LOWER(a.accountName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Account> searchAccountsByNameOrDescription(@Param("searchTerm") String searchTerm, Pageable pageable);
} 