package ru.capybarovsk.overhaul.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.capybarovsk.overhaul.dao.InvoiceDao;
import ru.capybarovsk.overhaul.model.User;

@Controller
@RequestMapping("/invoices")
public class InvoicesController {
    private final InvoiceDao invoiceDao;

    public InvoicesController(InvoiceDao invoiceDao) {
        this.invoiceDao = invoiceDao;
    }

    @GetMapping
    public String index(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("invoices", invoiceDao.getUserInvoices(user.id()));
        return "invoices";
    }
}
