public class Test {

  public int a;
  protected int b;
  public int c; // violation, variable access definition in wrong order

  Test() {
    this.a = 0;
  }

  public void foo() {
    // This method does nothing
  }

  Test(int a) { // violation, constructor definition in wrong order
    this.a = a;
  }

  private String name; // violation, instance variable declaration in wrong order
}
