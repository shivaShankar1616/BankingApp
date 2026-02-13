package com.example.banking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "transactions")    // no-args constructor
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long accountId;
    private String type;
    private double amount;
    private double balance;


    private LocalDateTime time = LocalDateTime.now();

    // 🔹 Custom constructor used in service
    public Transaction(Long accountId, String type, double amount, double balance) {
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
        this.balance = balance;
        this.time = LocalDateTime.now();
    }
}
