package com.example.Liderum.Controllers;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @GetMapping("/restricted")
    @PreAuthorize("hasRole('MARSHAL', 'GENERAL', 'MAJOR')")
    public String acessoRestrito() {
        return "Acesso concedido ao Staff!";
    }
}
