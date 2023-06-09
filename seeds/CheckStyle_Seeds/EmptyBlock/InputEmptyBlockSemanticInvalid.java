import java.io.*; // star import for instantiation tests
// explicit import for instantiation tests

/** Test case for detecting empty block statements. */
class InputEmptyBlockSemanticInvalid {
  static {
    Boolean x = new Boolean(true);
  }

  {
    Boolean x = new Boolean(true);
    Boolean[] y = new Boolean[] {Boolean.TRUE, Boolean.FALSE};
  }

  Boolean getBoolean() {
    return new java.lang.Boolean(true);
  }

  void exHandlerTest() {
    try {
    } finally {
    }
    try {
      // something
    } finally {
      // something
    }
    try { // ok
      ; // something
    } finally { // ok
      ; // statement
    }
  }

  /** test * */
  private static final long IGNORE = 666l + 666L;

  public class EqualsVsHashCode1 {
    public boolean equals(int a) {
      return a == 1;
    }
  }

  // empty instance initializer
  {
  }

  private class InputBraces {}

  synchronized void foo() {
    synchronized (this) {
    }
    synchronized (Class.class) { // ok
      synchronized (new Object()) {
        // text
      }
    }
  }

  static {
    int a = 0;
  }

  static {
  }
}
