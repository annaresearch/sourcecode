/*
InnerTypeLast


*/


public class InputInnerTypeLastClassCtorsInitBlocks {
  public class Inner {}

  public InputInnerTypeLastClassCtorsInitBlocks() { // violation
  }
}

class BeforeInitBlock {

  public class Inner2 {}

  {
  } // violation
}

class BeforeStaticInitBlock {

  public interface Inner3 {}

  static {
  } // violation
}
