package se.juneday.lifegame.web.format;

import se.juneday.lifegame.domain.ThingAction;
import se.juneday.lifegame.domain.Suggestion;

import java.util.Map;
import java.util.List;

public class HTMLFormater implements Formater {

  public String title(String title) {
    return "<b>" + title + "</b>";
  }

  public String description(String description) {
    return "<b>" + description + "</b>";
  }

  public String things(Map<ThingAction, Integer> things) {
    String s = "";
    for (Map.Entry<ThingAction, Integer> entry : things.entrySet()) {
      s = s + entry.getKey() + "=" + entry.getValue();
    }
    return s;
  }

  public String actions(List<ThingAction> actions) {
    String s = "";
    for (ThingAction action : actions) {
      s = s + action;
    }
    return s;
  }

  public String suggestions(List<Suggestion> suggestions) {
    String s = "";
    for (Suggestion suggestion : suggestions) {
      s = s + suggestion;
    }
    return s;
  }
}
