/*
JavadocContentLocation
location = FIRST_LINE


*/


public interface InputJavadocContentLocationFirstLine {

  /** Text. // violation above 'Javadoc content should start from the same line as /\*\*.' */
  void violation();

  /** Text. // OK */
  void ok();
  // violation below 'Javadoc content should start from the same line as /\*\*.'
  /** Third line. */
  void thirdLineViolation();

  /** */
  void blankLinesOnly();

  /** */
  void missingAsterisks();

  /** **** Extra asterisks. //OK */
  void extraAsterisks();

  /**
   * @implNote Does nothing. // OK
   */
  void javadocTag();

  /** // OK HTML paragraph. */
  void htmlTag();

  /** Single line. // OK * */
  void singleLine();

  /*
   * Not a javadoc comment. // OK
   */
  void notJavadocComment();
}
