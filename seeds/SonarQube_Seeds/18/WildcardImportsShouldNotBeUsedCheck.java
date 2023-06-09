
import static java.util.Arrays.*; // Compliant, exception with static imports
import static java.util.Collections.addAll;

import java.io.*; // Noncompliant [[sc=8;ec=17]] {{Explicitly import the specific classes needed.}}
import java.util.*; // Noncompliant {{Explicitly import the specific classes needed.}}
import org.apache.commons.io.*; // Noncompliant {{Explicitly import the specific classes needed.}}
// Not used in code but at least one non static import

/** A test class */
public final class WildcardImportsShouldNotBeUsedCheck {

  /** Constructor */
  private WildcardImportsShouldNotBeUsedCheck() {
    super();
  }

  /**
   * java.io declaration
   *
   * @param input Input
   * @return null or not
   */
  public static boolean testMethodInput(InputStream input) {
    return input == null;
  }

  /** java.util declaration */
  public static void testMethodArrays() {
    sort(new int[] {});
    List<Class<?>> list = new ArrayList<Class<?>>();
    addAll(list, FileUtils.class);
  }
}
