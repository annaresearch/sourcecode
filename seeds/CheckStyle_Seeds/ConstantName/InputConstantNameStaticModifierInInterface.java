/*
ConstantName
format = (default)^[A-Z][A-Z0-9]*(_[A-Z0-9]+)*$
applyToPublic = (default)true
applyToProtected = (default)true
applyToPackage = (default)true
applyToPrivate = (default)true


*/


public interface InputConstantNameStaticModifierInInterface // ok
 {
  static int f() {
    int someName = 5;
    return someName;
  }
}
