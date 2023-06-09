/*
InterfaceIsType
allowMarkerInterfaces = (default)true


*/


/**
 * Test input for InterfaceIsTypeCheck
 *
 * @author lkuehne
 */
class InputInterfaceIsType {
  // OK, has method, so is a type
  interface OK {
    void method();
  }

  // Marker interface, OK for some configurations
  interface Marker {}

  // Always flagged
  interface ConstantPool // violation
   {
    boolean BAD = true;
  }
}
