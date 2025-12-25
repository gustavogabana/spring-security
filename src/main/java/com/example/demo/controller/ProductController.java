package com.example.demo.controller;

import com.example.demo.domain.product.Product;
import com.example.demo.domain.product.ProductRequestDTO;
import com.example.demo.domain.product.ProductResponseDTO;
import com.example.demo.repository.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController()
@RequestMapping("/product")
public class ProductController {

    private final ProductRepository repository;

    public ProductController(ProductRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public ResponseEntity<Void> postProduct(@RequestBody @Valid ProductRequestDTO body) {
        Product newProduct = new Product(body);
        this.repository.save(newProduct);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> getAllProducts() {
        List<ProductResponseDTO> productList = this.repository.findAll().stream()
                .map(ProductResponseDTO::new)
                .toList();
        return ResponseEntity.ok(productList);
    }
}
