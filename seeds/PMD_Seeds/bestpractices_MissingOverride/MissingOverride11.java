
public enum EnumWithAnonClass {
  Foo {
    // missing
    public String toString() {
      return super.toString();
    }

    // missing
    public String getSomething() {
      return null;
    }
  };

  public Object getSomething() {
    return null;
  }
}
