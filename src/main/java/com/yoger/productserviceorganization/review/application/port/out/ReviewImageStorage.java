package com.yoger.productserviceorganization.review.application.port.out;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface ReviewImageStorage {
    List<String> uploadImages(List<MultipartFile> images);

    void deleteImages(List<String> imageUrls);
}
