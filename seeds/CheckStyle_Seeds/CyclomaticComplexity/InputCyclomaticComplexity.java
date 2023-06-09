/*
CyclomaticComplexity
max = 0
switchBlockAsSingleDecisionPoint = (default)false
tokens = (default)LITERAL_WHILE, LITERAL_DO, LITERAL_FOR, LITERAL_IF, LITERAL_SWITCH, \
         LITERAL_CASE, LITERAL_CATCH, QUESTION, LAND, LOR


*/


public class InputCyclomaticComplexity {
  // NP = 2
  public void foo() { // violation
    // NP(while-statement) = (while-range=1) + (expr=0) + 1 = 2
    while (true) {
      Runnable runnable =
          new Runnable() {
            // NP = 2
            public void run() { // violation
              // NP(while-statement) = (while-range=1) + (expr=0) + 1 = 2
              while (true) {}
            }
          };

      new Thread(runnable).start();
    }
  }

  // NP = 10
  public void bar() { // violation
    // NP = (if-range=3*3) + (expr=0) + 1 = 10
    if (System.currentTimeMillis() == 0) {
      // NP = (if-range=1) + 1 + (expr=1) = 3
      if (System.currentTimeMillis() == 0 && System.currentTimeMillis() == 0) {}
      // NP = (if-range=1) + 1 + (expr=1) = 3
      if (System.currentTimeMillis() == 0 || System.currentTimeMillis() == 0) {}
    }
  }

  // NP = 3
  public void simpleElseIf() { // violation
    // NP = (if-range=1) + (else-range=2) + 0 = 3
    if (System.currentTimeMillis() == 0) {
      // NP(else-range) = (if-range=1) + (else-range=1) + (expr=0) = 2
    } else if (System.currentTimeMillis() == 0) {
    } else {
    }
  }

  // NP = 7
  public void stupidElseIf() { // violation
    // NP = (if-range=1) + (else-range=3*2) + (expr=0) = 7
    if (System.currentTimeMillis() == 0) {
    } else {
      // NP = (if-range=1) + (else-range=2) + (expr=0) = 3
      if (System.currentTimeMillis() == 0) {
      } else {
        // NP = (if-range=1) + 1 + (expr=0) = 2
        if (System.currentTimeMillis() == 0) {}
      }
      // NP = (if-range=1) + 1 + (expr=0) = 2
      if (System.currentTimeMillis() == 0) {}
    }
  }

  // NP = 3
  public InputCyclomaticComplexity() // violation
      {
    int i = 1;
    // NP = (if-range=1) + (else-range=2) + 0 = 3
    if (System.currentTimeMillis() == 0) {
      // NP(else-range) = (if-range=1) + (else-range=1) + (expr=0) = 2
    } else if (System.currentTimeMillis() == 0) {
    } else {
    }
  }

  // STATIC_INIT
  // NP = 3
  static { // violation
    int i = 1;
    // NP = (if-range=1) + (else-range=2) + 0 = 3
    if (System.currentTimeMillis() == 0) {
      // NP(else-range) = (if-range=1) + (else-range=1) + (expr=0) = 2
    } else if (System.currentTimeMillis() == 0) {
    } else {
    }
  }

  // INSTANCE_INIT
  // NP = 3
  { // violation
    int i = 1;
    // NP = (if-range=1) + (else-range=2) + 0 = 3
    if (System.currentTimeMillis() == 0) {
      // NP(else-range) = (if-range=1) + (else-range=1) + (expr=0) = 2
    } else if (System.currentTimeMillis() == 0) {
    } else {
    }
  }

  /** Inner */
  // NP = 0
  public InputCyclomaticComplexity(int aParam) // violation
      {
    Runnable runnable =
        new Runnable() {
          // NP = 2
          public void run() { // violation
            // NP(while-statement) = (while-range=1) + (expr=0) + 1 = 2
            while (true) {}
          }
        };
    new Thread(runnable).start();
  }
}
