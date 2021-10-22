package io.bce.validation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Documented
@Retention(RUNTIME)
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE, ElementType.FIELD })
@Constraint(validatedBy = {AlwaysFailedValidator.class})
public @interface AlwaysFailed {
	String passedProperty();
	
	String message();

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

}
