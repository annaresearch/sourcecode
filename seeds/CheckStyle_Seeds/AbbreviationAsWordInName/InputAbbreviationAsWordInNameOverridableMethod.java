import org.junit.Before;

abstract class InputAbbreviationAsWordInNameOverridableMethod extends Class1 {
  public int serialNUMBER = 6;
  public final int s1erialNUMBER = 6;
  private static int s2erialNUMBER = 6;
  private static final int s3erialNUMBER = 6;

  @Override
  @SuppressWarnings(value = {""})
  @Before
  protected void oveRRRRRrriddenMethod() {
    int a = 0;
    // blah-blah
  }
}

class Class1 {
  @SuppressWarnings(value = {""})
  protected void oveRRRRRrriddenMethod() { // violation
    int a = 0;
    // blah-blah
  }
}

class Class2 extends Class1 {

  @Override
  @SuppressWarnings(value = {""})
  @Before
  protected void oveRRRRRrriddenMethod() {
    int a = 0;
    // blah-blah
  }
}
