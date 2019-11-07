package se.juneday.lifegame.web.format;

import se.juneday.lifegame.domain.ThingAction;
import se.juneday.lifegame.domain.Suggestion;

import java.util.Map;
import java.util.List;

import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;

public class HTMLFormater implements Formater {

  private String gameId;
  private final static String BOLD =          "<b>";
  private final static String BOLD_END =      "</b>";
  private final static String BR =            "<br>";
  private final static String LIST =          "<ul>";
  private final static String LIST_END =      "</ul>";
  private final static String LIST_ITEM =     "<li>";
  private final static String LIST_ITEM_END = "</li>";
  private final static String REF_1 =         "<a href=\"";
  private final static String REF_2 =         "\">";
  private final static String REF_END =       "</a>";
  private final static String H1 =            "<h1>";
  private final static String H1_END =        "</h1>";
  private final static String H2 =            "<h2>";
  private final static String H2_END =        "</h2>";

  private static final String DESCRIPTION = "";
  private static final String EXPLANATION = "Förklaring";
  private static final String SUGGESTIONS = "Vad gör du?";
  private static final String THINGS = "Dina saker (klicka för att lägga ned)";
  private static final String ACTION_THINGS = "Saker i rummet (klicka för att ta upp)";
  private static final String TITLE = "Rum";
  
  private static final String BASE_URL="/lifegame?format=html&gameId=";
  
  private String url() {
    return BASE_URL + gameId;
  }

  private String suggestionUrl(String suggestion) {
    try {
      return url() + "&suggestion=" + URLEncoder.encode(suggestion, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      // TODO: come on, handle properly
      return "";
    }
  }

  private String actionUrl(String action) {
    try {
      return url() + "&pickup=" + URLEncoder.encode(action, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      // TODO: come on, handle properly
      return "";
    }
  }

  private String thingUrl(String action) {
    try {
      return url() + "&drop=" + URLEncoder.encode(action, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      // TODO: come on, handle properly
      return "";
    }
  }

  public HTMLFormater(String gameId) {
    this.gameId = gameId;
  }
  
  public String start() {
    return "<!DOCTYPE html><html><head><meta charset=\"utf-8\"><title>LifeGame - web frontend</title></head><body>";
  }

  public String end() {
    return "</body></html>";
  }

  public String title(String title) {
    return H1 + title + H1_END;
  }

  public String description(String description) {
    return BOLD + DESCRIPTION + BOLD_END + BR + description + BR + BR;
  }

  public String things(Map<ThingAction, Integer> things) {
    if (things==null || things.size()==0) {
      return "";
    }
    String s = BOLD + THINGS + BOLD_END + LIST;
    for (Map.Entry<ThingAction, Integer> entry : things.entrySet()) {
      s = s + LIST_ITEM + REF_1 + thingUrl(entry.getKey().thing()) + REF_2 + entry.getKey().thing() + REF_END + LIST_ITEM_END;
    }
    return s + LIST_END;
  }

  public String actions(List<ThingAction> actions) {
    if (actions==null || actions.size()==0) {
      return "";
    }
    String s = BOLD + ACTION_THINGS + BOLD_END + LIST;
    for (ThingAction action : actions) {
      s = s + LIST_ITEM + REF_1 + actionUrl(action.thing()) + REF_2 + action.thing() + REF_END + LIST_ITEM_END;
    }
    return s + LIST_END;
  }

  public String suggestions(List<Suggestion> suggestions) {
    if (suggestions==null) {
      return "";
    }
    String s = BOLD + SUGGESTIONS + BOLD_END + LIST;
    for (Suggestion suggestion : suggestions) {
      s = s + LIST_ITEM + REF_1 + suggestionUrl(suggestion.phrase()) + REF_2 + suggestion.phrase() + REF_END + LIST_ITEM_END;
    }
    return s + LIST_END;
  }

  public String explanation(String explanation) {
    if (explanation==null) {
      return "";
    }
    return BOLD + EXPLANATION + BOLD_END + BR + explanation + BR;
  }

  public String win() {
    return "Victor is mine!!!";
  }

  public String invalidGameId() {
    return start() + H1 +
      "Invalid game!!" +
      H1_END + BR +
      "This is probably caused by either a bad game id or that the game has been inactive for too long" +
      BR + BR + BR +  BOLD +
      "Start a " + REF_1 +
      "/" + REF_2 +
      "new game" + REF_END + BOLD_END + end();
  }
  
}
