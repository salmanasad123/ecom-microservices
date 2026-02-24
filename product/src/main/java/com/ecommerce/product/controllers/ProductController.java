package com.ecommerce.product.controllers;


import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@RequestBody ProductRequest productRequest) {

        ProductResponse productResponse = productService.createProduct(productRequest);
        return new ResponseEntity<ProductResponse>(productResponse, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable(value = "id") Long id,
                                                         @RequestBody ProductRequest productRequest) {

        Optional<ProductResponse> productResponse = productService.updateProduct(id, productRequest);

        ResponseEntity<ProductResponse> productResponseResponseEntity = productResponse.map((ProductResponse response) -> {
            return ResponseEntity.ok(response);
        }).orElseGet(() -> {
            return ResponseEntity.notFound().build();
        });

        return productResponseResponseEntity;
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {

        List<ProductResponse> productResponseList = productService.getAllProducts();

        return ResponseEntity.ok(productResponseList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable(name = "id") Long id) {

        Optional<ProductResponse> productResponse = productService.getProductById(id);

        ResponseEntity<ProductResponse> responseEntity = productResponse.map((ProductResponse response) -> {
            return ResponseEntity.ok(response);
        }).orElseGet(() -> {
            return ResponseEntity.notFound().build();
        });

        return responseEntity;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable(value = "id") Long id) {

        boolean deleted = productService.deleteProduct(id);

        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> searchProducts(@RequestParam String keyword) {

        return ResponseEntity.ok(productService.searchProducts(keyword));
    }
}
