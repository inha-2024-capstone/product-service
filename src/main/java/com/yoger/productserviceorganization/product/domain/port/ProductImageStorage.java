package com.yoger.productserviceorganization.product.domain.port;

import org.springframework.web.multipart.MultipartFile;

public interface ProductImageStorage {
    String uploadImage(MultipartFile image);

    void deleteImage(String imageUrl);

    String updateImage(MultipartFile image, String originImageUrl);
}
