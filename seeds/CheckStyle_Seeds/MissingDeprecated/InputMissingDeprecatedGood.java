/*
MissingDeprecated
violateExecutionOnNonTightHtml = (default)false


*/


/**
 * @deprecated bleh
 */
@Deprecated
public class InputMissingDeprecatedGood // ok
 {
  /**
   * @deprecated bleh
   */
  @Deprecated
  protected InputMissingDeprecatedGood() {}

  /**
   * @deprecated bleh
   */
  @Deprecated
  @Override
  public String toString() {
    return "";
  }

  /**
   * @deprecated bleh
   */
  @Deprecated
  enum Rock {

    /**
     * @deprecated bleh
     */
    @Bleh2(bleh = 2)
    @Deprecated
    Metallica
  }
}

/**
 * @deprecated bleh
 */
@Deprecated
interface Foo5 {

  /**
   * @deprecated bleh
   */
  @Deprecated
  interface Bar {}
}

/**
 * @deprecated bleh
 */
@Deprecated
@interface Bleh6 {

  /**
   * @deprecated bleh
   */
  @Deprecated
  int bleh();
}

/**
 * @deprecated bleh
 */
@java.lang.Deprecated
@interface FullName {

  /**
   * @deprecated bleh
   */
  @java.lang.Deprecated
  int bleh();
}
