package com.example.banking.service;

import com.example.banking.entity.Account;
import com.example.banking.entity.Transaction;
import com.example.banking.repository.AccountRepository;
import com.example.banking.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AccountService {
    private final AccountRepository accountRepo;
    private final TransactionRepository txnRepo;

    private static final String UPLOAD_DIR =
            System.getProperty("user.dir") + "/uploads/";

    public AccountService(AccountRepository accountRepo,
                          TransactionRepository txnRepo) {
        this.accountRepo = accountRepo;
        this.txnRepo = txnRepo;
    }

    // ✅ CREATE ACCOUNT
    public Account createAccount(Account account,
                                 MultipartFile signature,
                                 MultipartFile photo) throws Exception {

        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) dir.mkdirs();

        String signaturePath = UPLOAD_DIR + System.currentTimeMillis()
                + "_" + signature.getOriginalFilename();

        String photoPath = UPLOAD_DIR + System.currentTimeMillis()
                + "_" + photo.getOriginalFilename();

        signature.transferTo(new File(signaturePath));
        photo.transferTo(new File(photoPath));

        account.setSignaturePath(signaturePath);
        account.setPhotoPath(photoPath);
        account.setBalance(0);

        try {
            return accountRepo.save(account);
        } catch (Exception e) {
            throw new RuntimeException(
                    "DB Error (Mobile / Aadhaar / PAN already exists)", e
            );
        }
    }

    // ✅ DEPOSIT
    public Account deposit(Long id, double amount) {

        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be greater than 0");
        }

        Account acc = getAccount(id);
        acc.setBalance(acc.getBalance() + amount);

        Transaction txn = new Transaction();
        txn.setAccountId(id);
        txn.setType("DEPOSIT");
        txn.setAmount(amount);
        txn.setBalance(acc.getBalance());
        txn.setTime(LocalDateTime.now());

        txnRepo.save(txn);
        return accountRepo.save(acc);
    }

    // ✅ WITHDRAW
    public Account withdraw(Long id, double amount) {

        if (amount <= 0) {
            throw new IllegalArgumentException("Withdraw amount must be greater than 0");
        }

        Account acc = getAccount(id);

        if (acc.getBalance() < amount) {
            throw new IllegalStateException("Insufficient balance");
        }

        acc.setBalance(acc.getBalance() - amount);

        Transaction txn = new Transaction();
        txn.setAccountId(id);
        txn.setType("WITHDRAW");
        txn.setAmount(amount);
        txn.setBalance(acc.getBalance());
        txn.setTime(LocalDateTime.now());

        txnRepo.save(txn);
        return accountRepo.save(acc);
    }



    // ✅ TRANSACTION HISTORY
    public List<Transaction> transactions(Long id) {
        return txnRepo.findByAccountId(id);
    }

    // 🔒 HELPER
    private Account getAccount(Long id) {
        return accountRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found with id " + id));
    }
    public double balance(Long id) {
        Account acc = accountRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        return acc.getBalance();
    }
    public List<Transaction> miniStatement(Long id) {

        accountRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        return txnRepo.findTop5ByAccountIdOrderByTimeDesc(id);
    }


}
