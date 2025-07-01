package com.example.demo.controller;

import com.example.demo.entity.Product;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ProductService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/user/products")
public class ProductController {

    private final ProductService productService;
    private final UserRepository userRepository;

    public ProductController(ProductService productService, UserRepository userRepository) {
        this.productService = productService;
        this.userRepository = userRepository;
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByUsername(auth.getName()).orElse(null);
    }

    @GetMapping
    public String listProducts(Model model) {
        User currentUser = getCurrentUser();
        model.addAttribute("products", productService.getProductsByOwner(currentUser));
        return "products";
    }

    @GetMapping("/add")
    public String addProductForm(Model model) {
        model.addAttribute("product", new Product());
        return "product-form";
    }

    @PostMapping("/add")
    public String addProduct(@ModelAttribute("product") Product product) {
        User currentUser = getCurrentUser();
        product.setOwner(currentUser);
        productService.saveProduct(product);
        return "redirect:/user/products";
    }

    @GetMapping("/edit/{id}")
    public String editProductForm(@PathVariable Long id, Model model) {
        Optional<Product> productOpt = productService.getProductById(id);
        if (productOpt.isPresent() && productOpt.get().getOwner().getUsername().equals(getCurrentUser().getUsername())) {
            model.addAttribute("product", productOpt.get());
            return "product-form";
        } else {
            return "redirect:/user/products";
        }
    }

    @PostMapping("/edit/{id}")
    public String updateProduct(@PathVariable Long id, @ModelAttribute("product") Product product) {
        User currentUser = getCurrentUser();
        product.setId(id);
        product.setOwner(currentUser);
        productService.saveProduct(product);
        return "redirect:/user/products";
    }

    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        Optional<Product> productOpt = productService.getProductById(id);
        if (productOpt.isPresent() && productOpt.get().getOwner().getUsername().equals(getCurrentUser().getUsername())) {
            productService.deleteProduct(id);
        }
        return "redirect:/user/products";
    }
}
