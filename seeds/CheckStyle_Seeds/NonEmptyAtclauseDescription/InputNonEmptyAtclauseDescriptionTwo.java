/*
NonEmptyAtclauseDescription
violateExecutionOnNonTightHtml = (default)false
javadocTokens = (default)PARAM_LITERAL, RETURN_LITERAL, THROWS_LITERAL, \
                EXCEPTION_LITERAL, DEPRECATED_LITERAL


*/


public class InputNonEmptyAtclauseDescriptionTwo {
  /**
   * @param a
   * @param b
   * @param c
   * @deprecated
   * @throws Exception
   */
  // violation 5 lines above
  // violation 5 lines above
  // violation 5 lines above
  // violation 5 lines above
  // violation 5 lines above
  public int foo4(String a, int b, double c) throws Exception {
    return 1;
  }

  /**
   * Some javadoc
   *
   * @param a Some javadoc
   * @param b Some javadoc
   * @param c Some javadoc
   * @return Some javadoc
   * @exception Exception Some javadoc
   * @deprecated Some javadoc
   */
  public int foo5(String a, int b, double c) throws Exception {
    return 1;
  }

  /**
   * @param a Some javadoc
   * @param b Some javadoc
   * @param c Some javadoc
   * @return Some javadoc
   * @exception Exception
   */
  // violation above
  public int foo6(String a, int b, double c) throws Exception {
    return 1;
  }

  /**
   * @param a xxx
   * @return
   */
  // violation above
  int foo(int a) {
    return a;
  }

  /**
   * @return {@code 1}
   */
  // ^ ok
  int bar() {
    return 1;
  }
}
