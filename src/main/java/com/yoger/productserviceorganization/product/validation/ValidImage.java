package com.yoger.productserviceorganization.product.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = ImageFileValidator.class)
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidImage {
    String message() default "지원하지 않는 파일 형식입니다. jpeg, png, gif 파일만 허용됩니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
