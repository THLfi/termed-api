package fi.thl.termed.util.spring.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.core.annotation.AliasFor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@RequestMapping(method = RequestMethod.PATCH, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
public @interface PatchJsonMapping {

  @AliasFor(annotation = RequestMapping.class) String name() default "";

  @AliasFor(annotation = RequestMapping.class) String[] value() default {};

  @AliasFor(annotation = RequestMapping.class) String[] path() default {};

  @AliasFor(annotation = RequestMapping.class) String[] params() default {};

  @AliasFor(annotation = RequestMapping.class) String[] headers() default {};

  @AliasFor(annotation = RequestMapping.class) String[] produces();

}
