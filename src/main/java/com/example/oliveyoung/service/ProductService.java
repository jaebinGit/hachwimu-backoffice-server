package com.example.oliveyoung.service;

import com.example.oliveyoung.model.Product;
import com.example.oliveyoung.repository.ProductRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final RedisTemplate<String, Object> redisTemplate;


    public ProductService(ProductRepository productRepository, RedisTemplate<String, Object> redisTemplate) {
        this.productRepository = productRepository;
        this.redisTemplate = redisTemplate;
    }

    // 상품 등록 처리 (쓰기 작업)
    @Transactional
    public Product createProduct(Product product) {
            // 데이터베이스에 상품 정보 저장 (쓰기 작업)
            Product savedProduct = productRepository.save(product);
            redisTemplate.delete("products:all");

            return savedProduct;
    }

    // 모든 상품 정보 조회 (읽기 작업)
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
            // 캐시에 없을 경우, 데이터베이스에서 모든 상품 조회
            List<Product> products = productRepository.findAll();

            return products;
    }

    // 상품 삭제 처리 (쓰기 작업)
    @Transactional
    public void deleteProduct(Long id) {
            // 상품 조회
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found!"));

            // 상품 삭제 처리
            productRepository.delete(product);
            redisTemplate.delete("products:all");
    }


    // 상품 업데이트 처리 (쓰기 작업)
    @Transactional
    public Product updateProduct(Long id, Product updatedProduct) {
            // 상품 조회
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found!"));

            // 상품 정보 업데이트
            product.setName(updatedProduct.getName());
            product.setImageUrl(updatedProduct.getImageUrl());
            product.setPrice(updatedProduct.getPrice());
            product.setBrand(updatedProduct.getBrand());
            product.setBest(updatedProduct.isBest());
            product.setDeliveryInfo(updatedProduct.getDeliveryInfo());
            product.setSaleStatus(updatedProduct.isSaleStatus());
            product.setCouponStatus(updatedProduct.isCouponStatus());
            product.setGiftStatus(updatedProduct.isGiftStatus());
            product.setTodayDreamStatus(updatedProduct.isTodayDreamStatus());
            product.setStock(updatedProduct.getStock());
            product.setDiscountPrice(updatedProduct.getDiscountPrice());
            product.setOtherDiscount(updatedProduct.isOtherDiscount());

            // 데이터베이스에 업데이트된 상품 정보 저장
            productRepository.save(product);
            redisTemplate.delete("products:all");

            return product;
    }
}