package com.yoger.productserviceorganization.review.adapter.web.dto.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public class ImageFilesValidator implements ConstraintValidator<ValidImages, List<MultipartFile>> {
    private boolean emptyable;

    @Override
    public void initialize(ValidImages constraintAnnotation) {
        this.emptyable = constraintAnnotation.emptyable();
    }

    @Override
    public boolean isValid(List<MultipartFile> files, ConstraintValidatorContext context) {
        files.forEach(
                file -> emptyable = isValidFile(file, context)
        );
        return emptyable;
    }

    private boolean isValidFile(MultipartFile file, ConstraintValidatorContext context) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }
        return contentType.equals("image/jpeg") ||
                contentType.equals("image/png") ||
                contentType.equals("image/gif");
    }
}
