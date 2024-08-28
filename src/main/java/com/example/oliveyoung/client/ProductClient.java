package com.example.oliveyoung.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(
        name = "service-products",
        url = "${feign.client.config.service-products.url:https://api.hachwimu.com}" // 동적으로 URL을 외부에서 설정 가능하도록 수정
)
public interface ProductClient {

    @PostMapping("/products/cache/clear")
    void clearAllProductsCache();
}