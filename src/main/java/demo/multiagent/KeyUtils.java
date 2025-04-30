package demo.multiagent;


public class KeyUtils {

  public static String readOpenAiKey() {
    return System.getenv("OPENAI_API_KEY");
  }


  public static boolean hasValidKeys() {
    try {
      return !readOpenAiKey().isEmpty();
    } catch (Exception e) {
      return false;
    }
  }

}
