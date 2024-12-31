package com.yoger.productserviceorganization.review.adapter.web.dto.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 이미지 파일의 형식을 검증하는 애노테이션입니다.
 * 지원되는 형식은 jpeg, png, gif입니다.
 * emptyable은 파일이 비어있어도 되는지 여부를 설정합니다.
 * 기본값은 false로, 파일이 비어있으면 검증 실패로 처리됩니다.
 */
@Constraint(validatedBy = ImageFilesValidator.class)
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ValidImages {
    String message() default "지원하지 않는 파일 형식입니다. jpeg, png, gif 파일만 허용됩니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    /**
     * 파일이 비어있어도 되는지 여부를 설정합니다.
     *
     * @return 빈 파일 허용 여부
     */
    boolean emptyable() default false;
}
