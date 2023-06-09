import java.io.*; // star import for instantiation tests
// explicit import for instantiation tests

/** Test case for detecting empty block statements. */
class InputEmptyBlockSemanticText {
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
    } // violation above 'Empty try block'
    finally {
    } // violation above 'Empty finally block'
    try { // ok
      // something
    } finally { // ok
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
  } // violation above 'Empty INSTANCE_INIT block'

  private class InputBraces {}

  synchronized void foo() {
    synchronized (this) {
    } // violation 'Empty synchronized block'
    synchronized (Class.class) { // ok
      synchronized (new Object()) { // ok
        // text
      }
    }
  }

  static {
    int a = 0;
  }
  // violation below 'Empty STATIC_INIT block'
  static {
  }
}
