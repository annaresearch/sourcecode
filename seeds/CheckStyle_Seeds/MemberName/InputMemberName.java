/*
MemberName
format = (default)^[a-z][a-zA-Z0-9]*$
applyToPublic = (default)true
applyToProtected = (default)true
applyToPackage = (default)true
applyToPrivate = (default)true


*/


public class InputMemberName {
  public int mPublic;
  protected int mProtected;
  int mPackage; // comment
  private int mPrivate;

  public int _public; // violation
  protected int _protected; // violation
  int _package; // violation
  private int _private; // violation
}
