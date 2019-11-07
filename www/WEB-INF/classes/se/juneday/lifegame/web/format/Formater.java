package se.juneday.lifegame.web.format;

import se.juneday.lifegame.domain.ThingAction;
import se.juneday.lifegame.domain.Suggestion;
import java.util.Map;
import java.util.List;

public interface Formater {

  String title(String title);
  String description(String description);
  String things(Map<ThingAction, Integer> things);
  String actions(List<ThingAction> actions);
  String suggestions(List<Suggestion> suggestions);
}
