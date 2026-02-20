package com.ecommerce.product.service;


import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.models.Product;
import com.ecommerce.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductResponse> getAllProducts() {

        List<Product> productList = productRepository.findByActiveTrue();

        List<ProductResponse> productResponseList = productList.stream()
                .map((Product product) -> {
                    return mapProductToProductResponse(product);
                }).collect(Collectors.toList());

        return productResponseList;
    }

    public ProductResponse createProduct(ProductRequest productRequest) {

        Product product = new Product();

        mapProductRequestToProduct(product, productRequest);

        Product savedProduct = productRepository.save(product);

        return mapProductToProductResponse(savedProduct);
    }

    public Optional<ProductResponse> updateProduct(Long id, ProductRequest productRequest) {

        return productRepository.findById(id)
                .map((Product existingProduct) -> {
                    mapProductRequestToProduct(existingProduct, productRequest);
                    Product updatedProduct = productRepository.save(existingProduct);
                    ProductResponse productResponse = mapProductToProductResponse(updatedProduct);
                    return productResponse;
                });
    }

    public Optional<ProductResponse> getProduct(Long id) {

        Optional<Product> optionalProduct = productRepository.findById(id);

        Optional<ProductResponse> productResponse = optionalProduct.map((Product product) -> {
            return mapProductToProductResponse(product);
        });

        return productResponse;

    }

    public boolean deleteProduct(Long id) {
        Boolean productDeleted = productRepository.findById(id)
                .map((Product foundProduct) -> {
                    foundProduct.setActive(false);
                    productRepository.save(foundProduct);
                    return true;
                })
                .orElse(false);

        return productDeleted;
    }

    public List<ProductResponse> searchProducts(String keyword) {

        return productRepository.searchProducts(keyword)
                .stream()
                .map((Product product) -> {
                  return mapProductToProductResponse(product);
                }).collect(Collectors.toList());
    }


    private ProductResponse mapProductToProductResponse(Product savedProduct) {

        ProductResponse productResponse = new ProductResponse();
        productResponse.setId(savedProduct.getId());
        productResponse.setCategory(savedProduct.getCategory());
        productResponse.setName(savedProduct.getName());
        productResponse.setDescription(savedProduct.getDescription());
        productResponse.setActive(savedProduct.getActive());
        productResponse.setImageUrl(savedProduct.getImageUrl());
        productResponse.setPrice(savedProduct.getPrice());
        productResponse.setStockQuantity(savedProduct.getStockQuantity());

        return productResponse;
    }

    private void mapProductRequestToProduct(Product product, ProductRequest productRequest) {

        product.setName(productRequest.getName());
        product.setCategory(productRequest.getCategory());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setImageUrl(productRequest.getImageUrl());
        product.setStockQuantity(productRequest.getStockQuantity());
    }


}
