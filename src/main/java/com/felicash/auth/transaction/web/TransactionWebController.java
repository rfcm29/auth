package com.felicash.auth.transaction.web;

import com.felicash.auth.transaction.*;
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
    private final CategoryService categoryService;

    public TransactionWebController(TransactionService transactionService, CategoryService categoryService) {
        this.transactionService = transactionService;
        this.categoryService = categoryService;
    }

    /**
     * Display all transactions with optional category type filter
     */
    @GetMapping
    public String listTransactions(
            @RequestParam(required = false) TransactionCategoryType categoryType,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        List<TransactionResponse> transactions;

        if (categoryType != null) {
            transactions = transactionService.getTransactionsByUserAndCategoryType(
                    userDetails.getUsername(), categoryType);
        } else {
            transactions = transactionService.getAllTransactionsByUser(userDetails.getUsername());
        }

        model.addAttribute("transactions", transactions);
        model.addAttribute("selectedCategoryType", categoryType);
        model.addAttribute("categoryTypes", TransactionCategoryType.values());

        return "transactions";
    }

    /**
     * Display form to create new transaction
     */
    @GetMapping("/new")
    public String newTransactionForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        List<Category> categories = categoryService.getAllCategoriesByUser(userDetails.getUsername());
        model.addAttribute("categories", categories);
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
