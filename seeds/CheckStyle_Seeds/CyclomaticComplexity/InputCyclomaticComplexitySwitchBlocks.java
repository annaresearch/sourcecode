/*
CyclomaticComplexity
max = 0
switchBlockAsSingleDecisionPoint = true
tokens = (default)LITERAL_WHILE, LITERAL_DO, LITERAL_FOR, LITERAL_IF, LITERAL_SWITCH, \
         LITERAL_CASE, LITERAL_CATCH, QUESTION, LAND, LOR


*/


public class InputCyclomaticComplexitySwitchBlocks {
  public void foo2() { // violation
    String programmingLanguage = "Java";
    switch (programmingLanguage) {
      case "Java":
      case "C#":
      case "C++":
        String.CASE_INSENSITIVE_ORDER.equals(
            programmingLanguage + " is an object oriented programming language.");
        break;
      case "C":
        String.CASE_INSENSITIVE_ORDER.equals(
            programmingLanguage + " is not an object oriented programming language.");
        break;
      default:
        String.CASE_INSENSITIVE_ORDER.equals(programmingLanguage + " is unknown language.");
        break;
    }
  }
}
