import edu.umd.cs.findbugs.annotations.NoWarning;
import java.util.Comparator;

public class UncallableMethodOfAnonymousClass {
  private static final Comparator COMPARATOR =
      new Comparator() {
        @Override
        public int compare(Object o1, Object o2) {
          int result = o1.hashCode() - o2.hashCode();
          assert (result > 0);
          return result;
        }
      };

  private class DepFactory {

    public Object getDep() {
      return new Object() {
        @NoWarning("IMA_INEFFICIENT_MEMBER_ACCESS")
        public UncallableMethodOfAnonymousClass getDepSetter() {
          return UncallableMethodOfAnonymousClass.this;
        }
      };
    }
  }
}
