package se.juneday.lifegame.web.format;

import se.juneday.lifegame.domain.ThingAction;
import se.juneday.lifegame.domain.Suggestion;
import java.util.Map;
import java.util.List;
import se.juneday.lifegame.web.EngineStore;
import se.juneday.lifegame.engine.LifeGameEngine;

import javax.servlet.*;
import javax.servlet.http.*;

public interface Formatter {

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
  
  String situation(String gameTitle,
                   String gameSubTitle,
                   String title,
                   String nick,
                   String explanation,
                   String description,
                   String question,
                   List<Suggestion> suggestions,
                   Map<ThingAction, Integer> things,
                   List<ThingAction> actions,
                   long millisLeft,
                   int situations,
                   int score);

                   

  
  String debug(String text);
  void debug(boolean enable);

  String games(int maxAge, EngineStore store);
  String worlds(List<Formatter.GameInfo> worlds);
  String error(String message);
  String info(HttpServletRequest request, HttpServletResponse response,String message);
  
  
  public class GameInfo {
    public String title;
    public String subTitle;
    public String url;
    public GameInfo(String title, String subTitle, String url) {
      this.title = title;
      this.subTitle = subTitle;
      this.url = url;
    }
  }
  
}
