import java.net.URL;

public class InputImportOrderBug { // ok
  // same as a class name
  private static String URL = "This is a String object";

  public InputImportOrderBug() throws Exception {
    URL url = new URL("file://this.is.a.url.object");
  }
}
