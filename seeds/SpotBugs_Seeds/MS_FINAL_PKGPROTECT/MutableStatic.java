import edu.umd.cs.findbugs.annotations.ExpectWarning;
import edu.umd.cs.findbugs.annotations.NoWarning;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MutableStatic {

  @ExpectWarning("MS_MUTABLE_COLLECTION_PKGPROTECT")
  public static final Hashtable h = new Hashtable();

  @ExpectWarning("MS_MUTABLE_COLLECTION_PKGPROTECT")
  public static final Map h2 = new Hashtable();

  @ExpectWarning("MS_PKGPROTECT")
  public static final int[] data = new int[5];

  @ExpectWarning("MS_FINAL_PKGPROTECT")
  public static int[] data2 = new int[5];

  @ExpectWarning("MS_SHOULD_BE_FINAL")
  public static Point p = new Point();

  @NoWarning("MS_MUTABLE_COLLECTION_PKGPROTECT")
  public static final List EMPTY_LIST = Arrays.asList();

  @ExpectWarning("MS_MUTABLE_COLLECTION_PKGPROTECT")
  public static final List LIST = Arrays.asList("a");

  @NoWarning("MS_MUTABLE_COLLECTION_PKGPROTECT")
  protected static final List PROPER_LIST = Collections.unmodifiableList(Arrays.asList("a"));

  @ExpectWarning("MS_MUTABLE_COLLECTION_PKGPROTECT")
  protected static final List EMPTY_ARRAY_LIST = new ArrayList(Arrays.asList());

  @ExpectWarning("MS_MUTABLE_COLLECTION_PKGPROTECT")
  protected static final List ARRAY_LIST = new ArrayList(Arrays.asList("a"));

  @ExpectWarning("MS_MUTABLE_COLLECTION_PKGPROTECT")
  public static final Set SET = new HashSet(Arrays.asList("a"));

  @NoWarning("MS_MUTABLE_COLLECTION_PKGPROTECT")
  public static final Set PROPER_SET = Collections.unmodifiableSet(new HashSet(Arrays.asList("a")));

  @ExpectWarning("MS_MUTABLE_COLLECTION_PKGPROTECT")
  public static final Map MAP = new HashMap();

  static {
    MAP.put("a", "b");
    MAP.put("c", "d");
  }

  @ExpectWarning("MS_MUTABLE_COLLECTION_PKGPROTECT")
  public static final Map MAP_ANONYMOUS =
      new HashMap() {
        {
          put("a", "b");
          put("c", "d");
        }
      };

  @NoWarning("MS_MUTABLE_COLLECTION_PKGPROTECT")
  public static final Map PROPER_MAP_ANONYMOUS =
      Collections.unmodifiableMap(
          new HashMap() {
            {
              put("a", "b");
              put("c", "d");
            }
          });

  public static void main(String... args) {
    System.out.println(namedPackage.MutableStaticInPackage.EMPTY_LIST);
    System.out.println(namedPackage.MutableStaticInPackage.LIST);
  }
}
