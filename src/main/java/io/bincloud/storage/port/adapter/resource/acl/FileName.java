package io.bincloud.storage.port.adapter.resource.acl;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Payload;
import javax.validation.constraints.Pattern;

@Documented
@Retention(RUNTIME)
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })

@Pattern.List({
	@Pattern(regexp = "\\S+", message = "resource.acl.filename.blank"),
	@Pattern(regexp = "^[^<>:;,?\"*|/]+$", message = "resource.acl.filename.wrong.format")
})
public @interface FileName {
	Class<?>[] groups() default {};
	String message() default "{javax.validation.constraints.NotNull.message}";
	Class<? extends Payload>[] payload() default {};
}
