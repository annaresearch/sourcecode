/*
JavadocContentLocation
location = (default)SECOND_LINE


*/


public interface InputJavadocContentLocationDefault {

  /** Text. // OK */
  void ok();

  /** Text. // violation 'Javadoc content should start from the next line after /\*\*.' */
  void violation();

  /**
   * // violation 'Javadoc content should start from the next line after /\*\*.'
   *
   * <p>Third line.
   */
  void thirdLineViolation();

  /** */
  void blankLinesOnly();

  /** */
  void missingAsterisks();

  /** Text. //OK More text. */
  void missingAsterisksWithText();

  /** *** Extra asterisks. //OK */
  void extraAsterisks();

  /**
   * @implNote Does nothing. // OK
   */
  void javadocTag();

  /** // OK HTML paragraph. */
  void htmlTag();

  /** Single line. * */
  // OK
  void singleLine();

  /***/
  // OK
  void emptyJavadocComment();

  /**/
  // OK
  void emptyComment();

  /* Not a javadoc comment. // OK
   */
  void notJavadocComment();
}
