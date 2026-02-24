package com.ecommerce.order.clients;

import com.ecommerce.order.dto.ProductResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

// this is product-service client as it will communication or call APIs of product .
// I have written an interface and spring automatically create a http client for that which calls other service.
@HttpExchange
public interface ProductServiceClient {

    @GetExchange("/api/products/{id}")
    ProductResponse getProductDetails(@PathVariable String id);

}
