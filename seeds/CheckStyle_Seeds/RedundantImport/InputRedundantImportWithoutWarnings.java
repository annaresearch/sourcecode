import static java.util.Arrays.asList;

import java.util.List;

public class InputRedundantImportWithoutWarnings { // ok
  private static final List<String> CONSTANTS = asList("a", "b");
}
