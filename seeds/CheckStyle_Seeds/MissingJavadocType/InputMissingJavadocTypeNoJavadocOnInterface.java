/*
MissingJavadocType
scope = PRIVATE
excludeScope = (default)null
skipAnnotations = (default)Generated
tokens = INTERFACE_DEF


*/


class InputMissingJavadocTypeNoJavadocOnInterface { // no violation,
  // CLASS_DEF not in the list of tokens
  interface NoJavadoc {} // violation
}
