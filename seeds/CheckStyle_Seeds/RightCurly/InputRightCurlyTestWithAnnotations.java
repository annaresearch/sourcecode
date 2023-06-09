/*
RightCurly
option = ALONE
tokens = LITERAL_TRY, LITERAL_CATCH, LITERAL_FINALLY, LITERAL_IF, LITERAL_ELSE, \
         CLASS_DEF, METHOD_DEF, CTOR_DEF, LITERAL_FOR, LITERAL_WHILE, LITERAL_DO, \
         STATIC_INIT, INSTANCE_INIT, ANNOTATION_DEF, ENUM_DEF


*/


class InputRightCurlyTestWithAnnotations {

  @Deprecated
  @Override
  public boolean equals(Object other) {
    boolean flag = true;
    return flag;
  } // violation

  @Override
  public String toString() {
    String s = "toString";
    return s;
  } // violation

  @Override
  @SuppressWarnings("unused")
  public int hashCode() {
    int a = 10;
    return 1;
  }

  @SuppressWarnings("unused")
  private void foo2() {
    int a = 9;
    return;
  } // violation

  @SuppressWarnings("unused")
  private void foo3() {
    int var1 = 5;
    var2 = 6;
  } // violation

  @Deprecated
  private void foo4() {
    return;
  } // violation

  @SuppressWarnings("unused")
  private int foo5() {
    return 1;
  } // violation

  @SuppressWarnings("unused")
  private String foo6() {
    return toString();
  }

  private String foo7() {
    String s = toString();
    return s.toString();
  } // violation

  private void foo8() {
    ;
    return;
  } // violation

  private int var1;
  private int v1;
  private int var2;
  private int v2;

  @SuppressWarnings("unused")
  public InputRightCurlyTestWithAnnotations() {
    this.var1 = 1;
  } // violation

  @SuppressWarnings("unused")
  public InputRightCurlyTestWithAnnotations(int v1, int v2) {
    this.var1 = v1;
    this.var2 = v2;
  }
  // violation above
  @SuppressWarnings("unused")
  private void foo9() {
    ;
    ;
  } // violation

  @SuppressWarnings("unused")
  private void foo10() {
    ;
  } // violation

  @SuppressWarnings("unused")
  private void foo11() {} // violation

  @SuppressWarnings("unused")
  private void foo12() {
    try {
      int i = 5;
      int b = 10;
    } // violation
    catch (Exception e) {
    } // violation
  }

  @Deprecated
  @SuppressWarnings("unused")
  private void foo13() {
    for (int i = 0; i < 10; i++) {
      int a = 5;
      int b = 6;
    } // violation

    do {
      var1 = 2;
    } while (var2 == 2);
  }

  static {
    int a;
    int b;
  } // violation

  static {
    int a;
  } // violation

  {
    int c;
    int d;
  } // violation

  {
    int c;
  } // violation

  @Deprecated
  private void foo14() {
    if (var1 > 0) {
      return;
    }
  }

  @Deprecated
  private void foo15() {
    class A {
      int a;
    }
    var1++; // violation
    class B {} // violation
    if (true) {

    } else
      ;
  }

  @Deprecated
  private void foo16() {
    if (true) {
      return;
    } else {
    } // 2 violations
    if (false) {}

    if (true) {
      return;
    } else {
    } // 2 violations
  }

  @Deprecated
  void foo17() {
    int v1 = 5;
    v2 = 6;
  }

  @Deprecated
  void foo18() {
    int v1 = 5;
    v2 = 6;
  }
  // 2 violations above
  private void foo19() {
    int var1 = 5;
    var2 = 6;
  } // violation

  @SuppressWarnings("Hello, world!")
  private String foo20() {
    do {
      var2++;
    } // violation
    while (var2 < 15);

    while (var1 < 10) {
      var1++;
    } // violation

    do {
      var2++;
      var1++;
    } while (var2 < 15);
    return "" + 0xCAFEBABE; // violation
  }

  private void foo21() {
    new Object() {
      @Override
      protected void finalize() {
        "".toString();
      }
    }; // violation
  }

  @SuppressWarnings("All")
  void foo22() {
    long startTime = System.nanoTime();
    try {
      int a = 5;
      toString();
    } catch (Exception e) { // violation
      throw new RuntimeException(e);
    } finally {
      toString();
    } // 2 violations
  }

  @SuppressWarnings("")
  void doDoubleBraceInitialization() {
    java.util.Map<String, String> map =
        new java.util.LinkedHashMap<String, String>() {
          {
            put("Hello", "World");
            put("first", "second");
            put("polygene", "lubricants");
            put("alpha", "betical");
          }
        }; // NO violation

    Thread t =
        new Thread() {
          @Override
          public void run() {
            super.run();
          }
        }; // violation
    new Object() {
      public int hashCode() {
        return 1;
      }

      {
        int a = 5;
      }
    }; // 2 violations
    new Object() {
      public int hashCode() {
        return 1;
      }

      int b = 10;
    }; // violation
    new Object() {
      public int hashCode() {
        return 1;
      }

      {
        int c = 5;
      }

      int d = 8;
    };
    // 2 violations above
    java.util.Map<String, String> map2 =
        new java.util.LinkedHashMap<String, String>() {
          {
            put("Hello", "World");
            put("first", "second");
            put("polygene", "lubricants");
            put("alpha", "betical");
          } // violation
        };

    java.util.Map<String, String> map3 =
        new java.util.LinkedHashMap<String, String>() {
          {
            put("Hello", "World");
            put("first", "second");
            put("polygene", "lubricants");
            put("alpha", "betical");
          }
        }; // violation

    java.util.Map<String, String> map4 =
        new java.util.LinkedHashMap<String, String>() {
          {
            put("Hello", "World");
            put("first", "second");
            put("polygene", "lubricants");
            put("alpha", "betical");
          }
        };

    foo23(
        new java.util.HashSet<String>() {
          {
            add("XZ13s");
            add("AB21/X");
            add("YYLEX");
            add("AR5E");
          }
        }); // violation

    foo23(
        new java.util.HashSet<String>() {
          {
            add("XZ13s");
            add("AB21/X");
            add("YYLEX");
            add("AR5E");
          }
        });
  } // 2 violations

  void foo23(java.util.HashSet<String> set) {}

  void foo25() {
    for (int i = 0; i < 10; i++) {
      System.identityHashCode("Hello, world!");
    }
  } // 2 violations

  void foo26() {
    for (int i = 0; i < 10; i++) {
      System.identityHashCode("Hello, world!");
    }
  } // 2 violations

  void foo27() {
    for (int i = 0; i < 10; i++) {
      for (int j = 0; j < 15; j++) {
        int a;
      }
    }
  } // 3 violations

  private java.util.ArrayList<Integer> foo28(int delta) {
    return new java.util.ArrayList<Integer>() {
      @Override
      public int size() {
        return Math.max(0, super.size() + 1);
      }
      ; // violation
    };
  }

  private void foo29() {
    boolean flag = true;
    if (flag) {
      System.identityHashCode("heh");
      flag = !flag;
    }
    String.CASE_INSENSITIVE_ORDER. // violation
        equals("Xe-xe");
  }

  public void testMethod() {}
  ; // violation

  public void testMethod1() {}
  ; // violation

  public class TestClass {}
  ; // violation

  public class TestClass1 {}
  ; // violation

  public class TestClass2 {
    public TestClass2() {}
    ; // violation

    public TestClass2(String someValue) {}
    ; // violation
  }

  public @interface TestAnnotation {} // violation

  public @interface TestAnnotation1 {
    String value();
  } // violation

  public @interface TestAnnotation2 {
    String value();
  } // violation

  public @interface TestAnnotation3 {
    String value();
  }

  public @interface TestAnnottation4 {
    String value();
  }

  public @interface TestAnnnotation5 {
    String someValue();
  }; // violation

  public @interface TestAnnotation6 {}; // violation

  public @interface TestAnnotation7 {
    String someValue();
  }; // violation

  public @interface TestAnnotation8 {
    String someValue();
  }; // violation

  public @interface TestAnnotation9 {
    String someValue();
  }; // violation
}
