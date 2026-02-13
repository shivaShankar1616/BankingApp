package com.example.banking.controller;

import com.example.banking.entity.Account;
import com.example.banking.entity.Transaction;
import com.example.banking.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bank")
public class AccountController {
    private final AccountService service;
    private final ObjectMapper mapper = new ObjectMapper();

    public AccountController(AccountService service) {
        this.service = service;
    }

    // ✅ 1. CREATE ACCOUNT (JSON + FILES)
    @PostMapping(value = "/create", consumes = "multipart/form-data")
    public Account createAccount(
            @RequestPart("account") String accountJson,
            @RequestPart("signature") MultipartFile signature,
            @RequestPart("photo") MultipartFile photo
    ) throws Exception {

        if (signature.isEmpty() || photo.isEmpty()) {
            throw new RuntimeException("Signature and Photo are mandatory");
        }

        Account account = mapper.readValue(accountJson, Account.class);
        return service.createAccount(account, signature, photo);
    }

    // ✅ 2. DEPOSIT
    @PutMapping("/{id}/deposit")
    public Account deposit(
            @PathVariable Long id,
            @RequestParam double amount
    ) {
        return service.deposit(id, amount);
    }

    // ✅ 3. WITHDRAW
    @PutMapping("/{id}/withdraw")
    public Account withdraw(
            @PathVariable Long id,
            @RequestParam double amount
    ) {
        return service.withdraw(id, amount);
    }


    // ✅ 5. TRANSACTION HISTORY
    @GetMapping("/{id}/transactions")
    public List<Transaction> transactions(@PathVariable Long id) {
        return service.transactions(id);
    }
    @GetMapping("/{id}/balance")
    public ResponseEntity<?> checkBalance(@PathVariable Long id) {
        double balance = service.balance(id);
        return ResponseEntity.ok(Map.of(
                "accountId", id,
                "balance", balance
        ));
    }
    @GetMapping("/{id}/mini-statement")
    public ResponseEntity<List<Transaction>> miniStatement(@PathVariable Long id) {
        return ResponseEntity.ok(service.miniStatement(id));
    }

}
