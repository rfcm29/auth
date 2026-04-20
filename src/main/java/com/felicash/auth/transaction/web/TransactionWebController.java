package com.felicash.auth.transaction.web;

import com.felicash.auth.transaction.CreateTransactionRequest;
import com.felicash.auth.transaction.TransactionCategory;
import com.felicash.auth.transaction.TransactionResponse;
import com.felicash.auth.transaction.TransactionService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/transactions")
public class TransactionWebController {

    private final TransactionService transactionService;

    public TransactionWebController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * Display all transactions with optional category filter
     */
    @GetMapping
    public String listTransactions(
            @RequestParam(required = false) TransactionCategory category,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        List<TransactionResponse> transactions;

        if (category != null) {
            transactions = transactionService.getTransactionsByUserAndCategory(
                    userDetails.getUsername(), category);
        } else {
            transactions = transactionService.getAllTransactionsByUser(userDetails.getUsername());
        }

        model.addAttribute("transactions", transactions);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("categories", TransactionCategory.values());

        return "transactions";
    }

    /**
     * Display form to create new transaction
     */
    @GetMapping("/new")
    public String newTransactionForm(Model model) {
        model.addAttribute("categories", TransactionCategory.values());
        return "create-transaction";
    }

    /**
     * Handle transaction creation form submission
     */
    @PostMapping("/new")
    public String createTransaction(
            @ModelAttribute CreateTransactionRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            transactionService.createTransaction(request, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("successMessage", "Transaction created successfully!");
            return "redirect:/transactions";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create transaction: " + ex.getMessage());
            return "redirect:/transactions/new";
        }
    }
}
