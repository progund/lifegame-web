package se.juneday.lifegame.web.format;

import se.juneday.lifegame.domain.ThingAction;
import se.juneday.lifegame.domain.Suggestion;

import se.juneday.lifegame.web.EngineStore;
import se.juneday.lifegame.engine.LifeGameEngine;

import java.util.Map;
import java.util.List;

import java.time.Instant;
import java.time.Duration;

import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;


public class HTMLFormater implements Formater {

  private String gameId;
  private final static String BOLD =          "<b>";
  private final static String BOLD_END =      "</b>";
  private final static String BR =            "<br>";
  private final static String HR =            "<hr>";
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
  private final static String H3 =            "<h3>";
  private final static String H3_END =        "</h3>";
  private final static String H4 =            "<h4>";
  private final static String H4_END =        "</h4>";

  private static final String DESCRIPTION = "";
  private static final String EXPLANATION = "Förklaring";
  private static final String SUGGESTIONS = "Vad gör du?";
  private static final String THINGS = "Dina saker";
  private static final String THINGS_SUB = "Klicka för att lägga ned";
  private static final String ACTION_THINGS = "Saker i rummet";
  private static final String ACTION_THINGS_SUB = "Klicka för att plocka upp";
  private static final String TITLE = "Rum";
  
  private static final String BASE_URL="/lifegame?format=html&gameId=";

  private String debugParam = "";
  
  private String url() {
    return BASE_URL + gameId + debugParam;
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

  private String exitUrl() {
    return url() + "&exit=true";
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
    StringBuffer sb = new StringBuffer();
    sb.append("<!DOCTYPE html><html><head><meta charset=\"utf-8\"><title>LifeGame - web frontend</title>\n");
    sb.append("    <style>\n");
    sb.append("      body  {\n");
    sb.append("      background-image: url(\"jds-bg.jpg\");\n");
    sb.append("      background-color: #cccccc;\n");

    sb.append("      background-size: cover; \n");
    sb.append("      font-size: 18pt;\n");
    sb.append("      color: #FFF;\n");
    sb.append("      }\n");
    sb.append("      a {\n");
    sb.append("      color: #fff;\n");
    sb.append("      }\n");
    sb.append("      #all {\n");
    sb.append("      width: 100%;\n");
    sb.append("      height: 100%;\n");
    sb.append("      }\n");
    sb.append("      #game {\n");
    sb.append("      float: left;\n");
    sb.append("      width: 100%;\n");
    sb.append("      height: 100%;\n");
    sb.append("      }\n");
    sb.append("      #situation {\n");
    sb.append("      float: left;\n");
    sb.append("      width: 60%;\n");
    sb.append("      }\n");
    sb.append("      #you {\n");
    sb.append("      float: right;\n");
    sb.append("      width: 30%;\n");
    sb.append("      }\n");
    sb.append("      #bottom {\n");
    sb.append("      position:absolute; bottom:0px;\n");
    sb.append("      width: 100%;\n");
    sb.append("      vertical-align: middle;\n");
    sb.append("      text-align: center; \n");
    sb.append("      }\n");
    sb.append("    </style>\n");
    sb.append("\n");
    sb.append("</head><body>\n");
    return sb.toString();
  }

  public String end() {
    return "</body></html>";
  }

  private String title(String title) {
    return H1 + title + H1_END + "\n";
  }

  private String description(String description) {
    return BOLD + DESCRIPTION + BOLD_END + BR + description + BR + BR + "\n";
  }

  private String things(Map<ThingAction, Integer> things) {
    if (things==null) {
      return "";
    }
    StringBuffer sb = new StringBuffer();
    sb.append(H2 + THINGS + H2_END);
    if (things.size()>0) {
      sb.append(H4 + THINGS_SUB + H4_END);
    }
    sb.append(LIST);
    for (Map.Entry<ThingAction, Integer> entry : things.entrySet()) {
      sb.append(LIST_ITEM + REF_1 + thingUrl(entry.getKey().thing()) + REF_2 + entry.getKey().thing() + REF_END + LIST_ITEM_END);
    }
    sb.append(LIST_END);
    sb.append("\n");
    return sb.toString();
  }

  private String actions(List<ThingAction> actions) {
    if (actions==null){
      return "";
    }
    StringBuffer sb = new StringBuffer();
    sb.append(H2 + ACTION_THINGS + H2_END);
    if (actions.size()>0) {
      sb.append(H4 + ACTION_THINGS_SUB + H4_END);
    }
    sb.append(LIST);
    for (ThingAction action : actions) {
      sb.append(LIST_ITEM + REF_1 + actionUrl(action.thing()) + REF_2 + action.thing() + REF_END + LIST_ITEM_END);
    }
    sb.append(LIST_END);
    return sb.toString();
  }

  private String suggestions(List<Suggestion> suggestions) {
    if (suggestions==null) {
      return "";
    }
    String s = BOLD + SUGGESTIONS + BOLD_END + LIST;
    for (Suggestion suggestion : suggestions) {
      s = s + LIST_ITEM + REF_1 + suggestionUrl(suggestion.phrase()) + REF_2 + suggestion.phrase() + REF_END + LIST_ITEM_END  + "\n";
    }
    return s + LIST_END + "\n";
  }

  private String explanation(String explanation) {
    if (explanation==null) {
      return "";
    }
    return BOLD + EXPLANATION + BOLD_END + BR + explanation + BR + "\n";
  }

  public String win() {
    return start() + "<div id=\"all\">" + H1 + "Victor is mine!!!" + H1_END + "</div>" + smallFooter() + end();
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

  public String debug(String text) {
    return BR + BR + BR + H4 + text + H4_END ;
  }

  
  
  public String games(EngineStore store) {
    String s = start();
    synchronized(store.engines()) {
      s += H1 + "Ongoing games: " + store.engines().size() + H1_END;
      for (Map.Entry<String, EngineStore.EngineStoreModel> entry : store.engines().entrySet()) {
        String inner;
        EngineStore.EngineStoreModel model = entry.getValue();
        LifeGameEngine engine = model.engine;
        Instant instant = model.lastUse;
        inner = H2 + entry.getKey() + H2_END;
        Duration res = Duration.between(entry.getValue().lastUse, Instant.now());
        inner += "Last use: " + res.toMinutes() + " minutes" + BR;
        inner += "Current situation: " + engine.situation().title() + BR;
        inner += "Score: " + engine.score() + BR;
        inner += "Situation count: " + engine.situationCount() + BR;
        inner += "Things: " + engine.things() + BR;
        // out.print(formater.actions(here.actions()));
        // out.print(formater.things(engine.things()));
        s += inner + "\n";
      }

      s += end();

      return s + "\n";
    }
  }

  public void debug(boolean enable) {
    if (enable) {
      debugParam = "&debug=true";
    } else {
      debugParam = "";
    }
  }

  public String footer() {
    StringBuffer sb = new StringBuffer();
    sb.append("    <div id=\"bottom\">\n");
    sb.append("      <h4>[<a href=\"/lifegame?format=html&gameId=155.4.69.33-1002-1573414556241&exit=true\"> avsluta spelet</a>");
    sb.append(" | <a href=\"www.juneday.se\">juneday.se</a>]</h4>\n");
    sb.append("    </div>\n");
    return sb.toString();
  }
  
  public String smallFooter() {
    StringBuffer sb = new StringBuffer();
    sb.append("    <div id=\"bottom\">\n");
    sb.append("      <h4>[ <a href=\"/\"> tillbaka till start </a>");
    sb.append(" | <a href=\"www.juneday.se\">juneday.se</a>]</h4>\n");
    sb.append("    </div>\n");
    return sb.toString();
  }
  
  public String situation(String title,
                          String explanation,
                          String description,
                          List<Suggestion> suggestions,
                          Map<ThingAction, Integer> things,
                          List<ThingAction> actions) {
    StringBuffer sb = new StringBuffer();
    sb.append("<div id=\"all\" >\n");
    sb.append(start());
    sb.append(H1 + title(title) + H1_END);
    sb.append("<div id=\"game\" >\n");
    sb.append("  <div id=\"situation\" >\n");
    sb.append(explanation(explanation));
    sb.append(description(description));
    sb.append(suggestions(suggestions));
    sb.append(actions(actions));
    sb.append("  </div>");
    sb.append("  <div id=\"you\" >\n");
    sb.append(things(things));
    sb.append("</div>");
    sb.append("</div>");
    sb.append(footer());
    sb.append(end());
    sb.append("</div>");
    return sb.toString();
  }


}
