/*
InnerTypeLast


*/


public enum InputInnerTypeLastClassRootClass { // ok
  ALWAYS(Bits.YES),
  NEVER(Bits.NO);

  private interface Bits {
    public static final int YES = 1;

    public static final int NO = 4;
  }

  private final int bits;

  private InputInnerTypeLastClassRootClass(int bits) {
    this.bits = bits;
  }
}
