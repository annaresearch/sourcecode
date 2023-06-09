/*
MissingJavadocMethod
minLineCount = 2
allowedAnnotations = MyAnnotation, Override
scope = private
excludeScope = (default)null
allowMissingPropertyJavadoc = (default)false
ignoreMethodNamesRegex = (default)null
tokens = (default)METHOD_DEF, CTOR_DEF, ANNOTATION_FIELD_DEF, COMPACT_CTOR_DEF


*/


/**
 * The following is a bad tag.
 *
 * @mytag Hello
 */
public class InputMissingJavadocMethodSmallMethods extends Some {
  // with comments

  /**
   * Some javadoc.
   *
   * @return Some javadoc.
   */
  int foo1() {
    return 1;
  }

  /**
   * Some javadoc.
   *
   * @return Some javadoc.
   */
  String foo2() {
    return "Fooooooooooooooo" + "ooooo" + "ooo";
  }

  /** Some javadoc. */
  void foo3() {
    foo2();
  }

  /** Some javadoc. */
  void foo4() {}

  // without comments

  int foo5() {
    return 1;
  }

  String foo6() {
    return "Fooooooooooooooo" + "oooooooo";
  }

  String foo7() // violation
      {
    return "Fooooooooooooooo" + "ooooo" + "ooo";
  }

  void foo8() {
    foo2();
  }

  void foo9() {}

  @MyAnnotation
  String foo10() {
    return "Fooooooooooooooo" + "ooooo" + "ooo";
  }

  @Override
  protected String foo11() {
    return "Fooooo" + "ooo" + "ooooooo" + "ooooo" + "ooo";
  }
}

@interface MyAnnotation {}

class Some {
  protected String foo11() {
    return "4";
  }
}
