/*
NPathComplexity
max = 0


*/


public class InputNPathComplexityDefault {
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
  public InputNPathComplexityDefault() // violation
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
  public InputNPathComplexityDefault(int aParam) {
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

  public void InputNestedTernaryCheck() { // violation
    double x = (getSmth() || Math.random() == 5) ? null : (int) Math.cos(400 * (10 + 40)); // good
    double y =
        (0.2 == Math.random())
            ? (0.3 == Math.random()) ? null : (int) Math.cos(400 * (10 + 40))
            : 6; // bad (nested in first position)
    double z =
        (Integer)
            ((0.2 == Math.random())
                ? (Integer) null + apply(null)
                : (0.3 == Math.random())
                    ? (Integer) null
                    : (int) Math.sin(300 * (12 + 30))); // bad (nested in second
    // position)
  }

  public boolean getSmth() {
    return true;
  }
  ; // violation

  public int apply(Object o) {
    return 0;
  } // violation

  public void inClass(int type, Short s, int color) {
    switch (type) {
      case 3:
        new Object() {
          public void anonymousMethod() { // violation
            {
              switch (s) {
                case 5:
                  switch (type) {
                    default:
                  }
              }
            }
          }
        };
      default:
        new Object() {
          class SwitchClass {
            { // violation
              switch (color) {
                case 5:
                  switch (type) {
                    default:
                  }
              }
            }
          }
        };
    }
  }
}
