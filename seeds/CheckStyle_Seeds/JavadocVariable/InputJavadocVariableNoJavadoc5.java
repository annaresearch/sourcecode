/*
JavadocVariable
scope = (default)private
excludeScope = (default)null
ignoreNamePattern =
tokens = (default)ENUM_CONSTANT_DEF


*/


public class InputJavadocVariableNoJavadoc5 // comment test
 {
  public int i1; // violation
  protected int i2; // violation
  int i3; // violation
  private int i4; // violation

  public void foo1() {}

  protected void foo2() {}

  void foo3() {}

  private void foo4() {}

  protected class ProtectedInner {
    public int i1; // violation
    protected int i2; // violation
    int i3; // violation
    private int i4; // violation

    public void foo1() {}

    protected void foo2() {}

    void foo3() {}

    private void foo4() {}
  }

  class PackageInner {
    public int i1; // violation
    protected int i2; // violation
    int i3; // violation
    private int i4; // violation

    public void foo1() {}

    protected void foo2() {}

    void foo3() {}

    private void foo4() {}
  }

  private class PrivateInner {
    public int i1; // violation
    protected int i2; // violation
    int i3; // violation
    private int i4; // violation

    public void foo1() {}

    protected void foo2() {}

    void foo3() {}

    private void foo4() {}
  }
}

class PackageClass5 {
  public int i1; // violation
  protected int i2; // violation
  int i3; // violation
  private int i4; // violation

  public void foo1() {}

  protected void foo2() {}

  void foo3() {}

  private void foo4() {}

  public class PublicInner {
    public int i1; // violation
    protected int i2; // violation
    int i3; // violation
    private int i4; // violation

    public void foo1() {}

    protected void foo2() {}

    void foo3() {}

    private void foo4() {}
  }

  protected class ProtectedInner {
    public int i1; // violation
    protected int i2; // violation
    int i3; // violation
    private int i4; // violation

    public void foo1() {}

    protected void foo2() {}

    void foo3() {}

    private void foo4() {}
  }

  class PackageInner {
    public int i1; // violation
    protected int i2; // violation
    int i3; // violation
    private int i4; // violation

    public void foo1() {}

    protected void foo2() {}

    void foo3() {}

    private void foo4() {}
  }

  private class PrivateInner {
    public int i1; // violation
    protected int i2; // violation
    int i3; // violation
    private int i4; // violation

    public void foo1() {}

    protected void foo2() {}

    void foo3() {}

    private void foo4() {}
  }

  class IgnoredName {
    // ignore by name
    private int logger; // violation
    // no warning, 'serialVersionUID' fields do not require Javadoc
    private static final long serialVersionUID = 0;
  }

  /**/
  void methodWithTwoStarComment() {}
}
