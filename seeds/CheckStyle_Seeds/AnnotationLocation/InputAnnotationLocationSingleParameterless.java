import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Target;

class InputAnnotationLocationSingleParameterless {

  @Annotation
  void singleParameterless() {}

  @Annotation
  @Annotation
  void multipleParameterless() {} // violation

  @Annotation("")
  void parameterized() {} // violation

  @Annotation(value = "")
  void namedParameterized() {} // violation

  @Annotation
  @Annotation("")
  @Annotation(value = "")
  void multiple() {} // 2 violations

  @Annotation("")
  @Annotation(value = "")
  void multipleParametrized() {} // 2 violations

  void parameterlessSamelineInForEach() {
    for (@Annotation Object o : new Object[0]) break; // ok
    for (@Annotation @Annotation Object o : new Object[0]) break; // ok
    for (@Annotation Object o; ; ) break; // ok
    for (@Annotation @Annotation Object o; ; ) break; // ok
  }

  @Repeatable(Annotations.class)
  @Target({ElementType.METHOD, ElementType.LOCAL_VARIABLE})
  @interface Annotation {
    String value() default "";
  }

  @Target({ElementType.METHOD, ElementType.LOCAL_VARIABLE})
  @interface Annotations {
    Annotation[] value();
  }
}
