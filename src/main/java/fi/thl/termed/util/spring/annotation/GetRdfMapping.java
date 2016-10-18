package fi.thl.termed.util.spring.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@RequestMapping(method = RequestMethod.GET, produces = {"application/n-triples;charset=UTF-8",
                                                        "application/rdf+xml;charset=UTF-8",
                                                        "application/json+ld;charset=UTF-8",
                                                        "text/turtle;charset=UTF-8",
                                                        "text/n3;charset=UTF-8"})
public @interface GetRdfMapping {

  @AliasFor(annotation = RequestMapping.class) String name() default "";

  @AliasFor(annotation = RequestMapping.class) String[] value() default {};

  @AliasFor(annotation = RequestMapping.class) String[] path() default {};

  @AliasFor(annotation = RequestMapping.class) String[] params() default {};

  @AliasFor(annotation = RequestMapping.class) String[] headers() default {};

  @AliasFor(annotation = RequestMapping.class) String[] consumes() default {};

}