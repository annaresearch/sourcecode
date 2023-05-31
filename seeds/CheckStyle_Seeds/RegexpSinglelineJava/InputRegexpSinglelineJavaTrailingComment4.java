/*
RegexpSinglelineJava
format = c-style\\s1
message = (default)(null)
ignoreCase = (default)false
minimum = (default)0
maximum = (default)0
ignoreComments = (default)false


*/


public class InputRegexpSinglelineJavaTrailingComment4 {
  int i; // don't use trailing comments :)
  // it fine to have comment w/o any statement
  /* good c-style comment. */
  int j; /* bad c-style comment. */

  void method1() {
    /* some c-style multi-line
    comment*/
    Runnable r =
        (new Runnable() {
          public void run() {}
        }); /* we should allow this */
  } // we should allow this
  /*
    Let's check multi-line comments.
  */
  /* c-style */
  // cpp-style // violation below
  /* c-style 1 */
  /*c-style 2 */

  void method2(long ms /* we should ignore this */) {
    /* comment before text */ int z;
    /* int y */ int y /**/;
  }

  /** comment with trailing space */
  public static final String NAME = "Some Name"; // NOI18N
}
