class MyClass {
  public void firstMethod1() {} // OK

  protected void secondMethod() {} // OK

  private void ThirdMethod() {} // violation, method name must match to the
  // default pattern '^[a-z][a-zA-Z0-9]*$'
  public void fourth_Method4() {} // violation, method name must match to the
  // default pattern '^[a-z][a-zA-Z0-9]*$'
}
