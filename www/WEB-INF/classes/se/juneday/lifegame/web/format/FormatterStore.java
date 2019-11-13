package se.juneday.lifegame.web.format;

public class FormatterStore {

  public static Formatter getStore(String format, String gameId) {
    if (format.equals("html")) {
      return new HTMLFormatter(gameId);
    } else if (format.equals("json")) {
      return new JSONFormatter(gameId);
    } 
    return null;
  }
  
}
