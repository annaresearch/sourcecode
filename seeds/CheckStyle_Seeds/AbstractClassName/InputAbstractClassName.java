public abstract class InputAbstractClassName { // violation
}

abstract class NonAbstractClassName { // violation
}

abstract class AbstractClassOther { // ok
  abstract class NonAbstractInnerClass { // violation
  }
}

class NonAbstractClass { // ok
}

class AbstractClass { // ok
}

abstract class AbstractClassName2 { // ok
  class AbstractInnerClass { // ok
  }
}
