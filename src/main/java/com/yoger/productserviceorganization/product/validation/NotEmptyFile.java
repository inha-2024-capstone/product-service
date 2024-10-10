package com.yoger.productserviceorganization.product.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NotEmptyFileValidator.class)
public @interface NotEmptyFile {
    String message() default "상품에 대한 사진을 첨부해주세요.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
