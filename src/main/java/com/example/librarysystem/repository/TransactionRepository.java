package com.example.librarysystem.repository;

import com.example.librarysystem.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserId(Long userId);
    List<Transaction> findByReturnDateBeforeAndStatus(LocalDate date, String status);
}

