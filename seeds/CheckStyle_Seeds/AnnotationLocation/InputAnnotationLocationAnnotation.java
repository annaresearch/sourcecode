import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Target;

@AnnotationAnnotation(value = "foo")
@AnnotationAnnotation // violation
@AnnotationAnnotation("bar")
@interface InputAnnotationLocationAnnotation { // violation

  @AnnotationAnnotation(value = "foo")
  @AnnotationAnnotation // violation
  @AnnotationAnnotation("bar")
  String value(); // violation
}

@Repeatable(AnnotationAnnotations.class)
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
@interface AnnotationAnnotation {

  String value() default "";
}

@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
@interface AnnotationAnnotations {

  AnnotationAnnotation[] value();
}
