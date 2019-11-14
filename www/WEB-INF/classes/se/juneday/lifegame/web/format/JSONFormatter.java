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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONString;

public class JSONFormatter implements Formatter {

  private String gameId;
  
  public JSONFormatter(String gameId) {
    this.gameId = gameId;
  }
  
  private JSONObject title(String title) {
    JSONObject jo = new JSONObject();
    return jo;
  }

  private JSONObject description(String description) {
    JSONObject jo = new JSONObject();
    jo.put("title", description);
    return jo;
  }

  private JSONArray things(Map<ThingAction, Integer> things) {
    JSONArray jarray = new JSONArray();
    if (things==null) {
      return jarray;
    }
    for (Map.Entry<ThingAction, Integer> entry : things.entrySet()) {
      jarray.put(entry.getKey().thing());
    }
    return jarray;
  }

  private JSONArray actions(List<ThingAction> actions) {
    JSONArray jarray = new JSONArray();
    if (actions==null){
      return jarray;
    }
    for (ThingAction action : actions) {
      jarray.put(action.thing());
    }
    return jarray;
  }

  private JSONArray suggestions(List<Suggestion> suggestions) {
    JSONArray jarray = new JSONArray();
    if (suggestions==null) {
      return jarray;
    }
    for (Suggestion suggestion : suggestions) {
      jarray.put(suggestion.phrase());
    }
    return jarray;
  }

  private String explanation(String explanation) {
    if (explanation==null) {
      return "";
    }
    return explanation;
  }

  public String win() {
    JSONObject jo = new JSONObject();
    jo.put("end","Victor is mine!!!");
    return jo.toString();
  }

  public String invalidGameId() {
      JSONObject jo = new JSONObject();
      jo.put("error", "Game id no longer valid");
      return jo.toString();
  }

  public String debug(String text) {
    JSONObject jo = new JSONObject();
    jo.put("debug","Victor is mine!!!");
    return jo.toString();
  }

  
  
  public String games(EngineStore store) {
    JSONArray jarray = new JSONArray();
    synchronized(store.engines()) {
      for (Map.Entry<String, EngineStore.EngineStoreModel> entry : store.engines().entrySet()) {
        JSONObject jo = new JSONObject();
        
        EngineStore.EngineStoreModel model = entry.getValue();
        LifeGameEngine engine = model.engine;
        Instant instant = model.lastUse;
        jo.put("gameid", entry.getKey());
        Duration res = Duration.between(entry.getValue().lastUse, Instant.now());
        jo.put("lastuse", res.toMinutes());
        jo.put("currentsituation", engine.situation().title());
        jo.put("score", engine.score());
        jo.put("situationcount",engine.situationCount());
        jo.put("things", things(engine.things()));
        jarray.put(jo);
      }

    }
    return jarray.toString();
  }

  public void debug(boolean enable) {
  }

  public String situation(String gameTitle,
                          String gameSubTitle,
                          String title,
                          String explanation,
                          String description,
                          List<Suggestion> suggestions,
                          Map<ThingAction, Integer> things,
                          List<ThingAction> actions) {
    JSONObject jo = new JSONObject();
    jo.put("gametitle",gameTitle );
    jo.put("gamesubtitle",gameSubTitle );
    jo.put("gameid",gameId );
    jo.put("title",title );
    jo.put("explanation",explanation(explanation));
    jo.put("description",description);
    jo.put("suggestions",suggestions(suggestions));
    jo.put("actions",actions(actions));
    jo.put("things",things(things));
    return jo.toString();
  }

  public String worlds(List<Formatter.GameInfo> worlds) {
    JSONArray jarray = new JSONArray();
    for (Formatter.GameInfo world: worlds) {
      JSONObject jo = new JSONObject();
      jo.put("title", world.title);
      jo.put("subtitle", world.subTitle);
      jo.put("url", world.url);
      System.out.println(" adding: " + world.subTitle);
      jarray.put(jo);
    }
    return jarray.toString();
  }

  public  String error(String message) {
      JSONObject jo = new JSONObject();
      jo.put("error", message);
      return jo.toString();
  }

}
