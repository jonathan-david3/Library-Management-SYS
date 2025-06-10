package com.example.librarysystem.controller;

import com.example.librarysystem.model.Book;
import com.example.librarysystem.model.User;
import com.example.librarysystem.model.Transaction;
import com.example.librarysystem.repository.BookRepository;
import com.example.librarysystem.repository.UserRepository;
import com.example.librarysystem.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class LibraryController {

    @Autowired
    private BookRepository bookRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private TransactionRepository transactionRepo;

    // Add a book
    @PostMapping("/books")
    public Book addBook(@RequestBody Book book) {
        book.setAvailability(true);
        return bookRepo.save(book);
    }

    // Get all books
    @GetMapping("/books")
    public List<Book> getAllBooks() {
        return bookRepo.findAll();
    }

    // Search books
    @GetMapping("/books/search")
    public List<Book> searchBooks(@RequestParam String key, @RequestParam String type) {
        switch (type) {
            case "title": return bookRepo.findByTitleContaining(key);
            case "author": return bookRepo.findByAuthorContaining(key);
            case "category": return bookRepo.findByCategory(key);
            default: return List.of();
        }
    }

    // Add a user
    @PostMapping("/users")
    public User addUser(@RequestBody User user) {
        return userRepo.save(user);
    }

    // Get all users
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    // Issue book
    @PostMapping("/issue")
    public String issueBook(@RequestParam Long bookId, @RequestParam Long userId) {
        Book book = bookRepo.findById(bookId).orElse(null);
        if (book == null || !book.isAvailability()) return "Book not available";

        book.setAvailability(false);
        bookRepo.save(book);

        Transaction transaction = new Transaction();
        transaction.setBookId(bookId);
        transaction.setUserId(userId);
        transaction.setIssueDate(LocalDate.now());
        transaction.setStatus("issued");

        transactionRepo.save(transaction);
        return "Book issued successfully";
    }

    // Return book
    @PostMapping("/return")
    public String returnBook(@RequestParam Long transactionId) {
        Transaction transaction = transactionRepo.findById(transactionId).orElse(null);
        if (transaction == null || transaction.getStatus().equals("returned"))
            return "Invalid or already returned";

        Book book = bookRepo.findById(transaction.getBookId()).orElse(null);
        if (book != null) {
            book.setAvailability(true);
            bookRepo.save(book);
        }

        transaction.setReturnDate(LocalDate.now());
        transaction.setStatus("returned");
        transactionRepo.save(transaction);

        return "Book returned successfully";
    }

    // Get user borrowing history
    @GetMapping("/history/{userId}")
    public List<Transaction> userHistory(@PathVariable Long userId) {
        return transactionRepo.findByUserId(userId);
    }

    // Report: Overdue books (before today and not returned)
    @GetMapping("/overdue")
    public List<Transaction> overdueBooks() {
        return transactionRepo.findByReturnDateBeforeAndStatus(LocalDate.now(), "issued");
    }
}
