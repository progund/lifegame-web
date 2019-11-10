package se.juneday.lifegame.web.format;

import se.juneday.lifegame.domain.ThingAction;
import se.juneday.lifegame.domain.Suggestion;
import java.util.Map;
import java.util.List;
import se.juneday.lifegame.web.EngineStore;
import se.juneday.lifegame.engine.LifeGameEngine;

public interface Formater {

  //  String start();
  String win();
  //String end();
  String invalidGameId();

  /*
  String title(String title);
  String description(String description);
  String things(Map<ThingAction, Integer> things);
  String actions(List<ThingAction> actions);
  String suggestions(List<Suggestion> suggestions);
  String explanation(String explanation);
  */
  
  String situation(String title,
                   String explanation,
                   String description,
                   List<Suggestion> suggestions,
                   Map<ThingAction, Integer> things,
                   List<ThingAction> actions);

  
  String debug(String text);
  void debug(boolean enable);

  String games(EngineStore store);

  
  
}
