import java.util.ArrayList;
import java.util.List;

/**
 * @author Hannes Dorfmann
 */
public class Test {

  public String toLowerCase(String input) {
    return input.toLowerCase();
  }

  public void foo() {

    String bar = "ASDTTQJKBKBJ";

    String lowerCase = toLowerCase(bar);

    List<String> inputs = new ArrayList<>();
    inputs.add("ASDNL");
    inputs.add("NONOQ");

    inputs.stream().map( str -> str.toLowerCase());
  }
}
