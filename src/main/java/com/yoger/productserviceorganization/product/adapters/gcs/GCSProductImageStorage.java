package com.yoger.productserviceorganization.product.adapters.gcs;

import com.google.cloud.storage.Acl;
import com.google.cloud.storage.Acl.Role;
import com.google.cloud.storage.Acl.User;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.yoger.productserviceorganization.product.config.GcpProductProperties;
import com.yoger.productserviceorganization.product.domain.port.ProductImageStorage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Profile("gcp")
public class GCSProductImageStorage implements ProductImageStorage {
    private final Storage storage;
    private final GcpProductProperties gcpProductProperties;

    @Override
    public String uploadImage(MultipartFile image) {
        String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
        BlobId blobId = BlobId.of(gcpProductProperties.bucket(), fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(image.getContentType())
                .setAcl(generatePublicAcl())
                .build();

        try {
            storage.create(blobInfo, image.getBytes());
            return String.format("https://storage.googleapis.com/%s/%s", gcpProductProperties.bucket(), fileName);
        } catch (IOException e) {
            throw new RuntimeException("GCS에 파일 업로드 중 오류 발생: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("파일 처리 중 오류 발생: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteImage(String imageUrl) {
        String bucket = gcpProductProperties.bucket();
        String prefix = String.format("https://storage.googleapis.com/%s/", bucket);

        if (!imageUrl.startsWith(prefix)) {
            throw new IllegalArgumentException("잘못된 이미지 URL입니다: " + imageUrl);
        }

        String fileName = imageUrl.substring(prefix.length());
        BlobId blobId = BlobId.of(bucket, fileName);

        boolean deleted = storage.delete(blobId);
        if (!deleted) {
            throw new RuntimeException("파일이 존재하지 않거나 삭제할 수 없습니다: " + imageUrl);
        }
    }

    @Override
    public String updateImage(MultipartFile image, String originImageUrl) {
        if (image != null && !image.isEmpty()) {
            String newImageUrl = uploadImage(image);
            deleteImage(originImageUrl);
            return newImageUrl;
        }
        return originImageUrl;
    }

    private List<Acl> generatePublicAcl() {
        List<Acl> acls = new ArrayList<>();
        acls.add(Acl.of(User.ofAllUsers(), Role.READER));
        return acls;
    }
}
