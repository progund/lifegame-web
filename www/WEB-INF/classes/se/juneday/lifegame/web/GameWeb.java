package se.juneday.lifegame.web;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import javax.servlet.*;
import javax.servlet.http.*;
import static java.nio.charset.StandardCharsets.UTF_8;

import se.juneday.lifegame.domain.Exit;
import se.juneday.lifegame.domain.InvalidLifeException;
import se.juneday.lifegame.domain.Situation;
import se.juneday.lifegame.domain.Suggestion;
import se.juneday.lifegame.domain.ThingAction;
import se.juneday.lifegame.engine.LifeGameEngine;
import se.juneday.lifegame.verification.LifeVerifier;
import se.juneday.lifegame.verification.LifeVerifierException;
import se.juneday.lifegame.json.JParser;
import se.juneday.lifegame.util.Log;
import se.juneday.lifegame.web.format.Formatter;
import se.juneday.lifegame.web.format.FormatterStore;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONString;


public class GameWeb extends HttpServlet {

  private static EngineStore engineStore;
  private LifeGameEngine engine;
  private static int counter = 1000;
  private boolean debug ;

  public static final String LOG_TAG = EngineStore.class.getSimpleName();
  public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  
  static {
    engineStore = EngineStore.getInstance();
  }


  private String date() {
    return LocalDateTime.now().format(formatter);
  }

  private String prefix(HttpServletRequest request) {
    return date() + " @ " + request.getRemoteAddr() ;
  }
  
  private void debug(HttpServletRequest request, String msg) {
    Log.d(LOG_TAG, "" + prefix(request) + " [" + msg + "]");
  }

  private void info(HttpServletRequest request, String msg) {
    Log.i(LOG_TAG, prefix(request) + " [" + msg + "]");
  }

  private boolean correctClientAddress(HttpServletRequest request, String gameId) {
    // simple (well, very simple) test to verify exit game comes from
    // correct client
    //    debug(request, " check " + request.getRemoteAddr() + " contains " + gameId + " => " + gameId.contains(request.getRemoteAddr()));
    return gameId.contains(request.getRemoteAddr());
  }
  
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {

    request.setCharacterEncoding(UTF_8.name());
    response.setContentType("text/html;charset=" + UTF_8.name());
    response.setCharacterEncoding(UTF_8.name());

    Log.logLevel(Log.LogLevel.DEBUG);

    String gameId = request.getParameter("gameId");
    String worlds = request.getParameter("worlds");
    String format = request.getParameter("format");
    String suggestion = request.getParameter("suggestion");
    String actionThing = request.getParameter("pickup");
    String dropThing = request.getParameter("drop");
    String world = request.getParameter("world");
    String admin = request.getParameter("admin");
    String action = request.getParameter("action");
    String exit = request.getParameter("exit");
    String debugParam = request.getParameter("debug");

    if (format==null) {
      format = "html";
    }

    
    PrintWriter out = response.getWriter();
    Formatter formatter = FormatterStore.getStore(format, gameId);

    debug = debugParam!=null && debugParam.equals("true");
    formatter.debug(debug);

    if (action!=null && action.equals("situation")) {
      writeSituation(request, response, out, formatter, gameId);
    } else if (worlds!=null) {
      worlds(out, formatter);
    } else if (admin!=null) {
      admin(request, out, formatter);
    } else if (exit!=null && exit.equals("true")) {
      //TODO: exit game
      exit(request, response, out, formatter, gameId);
    } else if (world!=null) {
      newWorld(request, response, world, format);
    } else {
      handleGame(request, response, out, formatter, gameId, suggestion, actionThing, dropThing);
    }
    
    out.close();
  }

  private Formatter.GameInfo infoFromFile(Path file) {
    JSONObject jo ;
    String data = null;
    try {
      data = new String(Files.readAllBytes(Paths.get(file.toString())));
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
    jo = new JSONObject(data);
      String title = jo.getString("title");
      String subTitle = jo.getString("subtitle");
      //      String url = "/lifegame?world=" + file.getFileName().toString().replace(".json","");
      String url = file.getFileName().toString().replace(".json","");
      return new Formatter.GameInfo(title, subTitle, url);
  }
  
  private void worlds(PrintWriter out, Formatter formatter) {
    Log.d(LOG_TAG, "worlds():   ");
    List<Formatter.GameInfo> worlds = new ArrayList<>();
    try {
      Files.newDirectoryStream(Paths.get("www/WEB-INF/data/"),
                               path -> path.toString().endsWith(".json"))
        .forEach((file) ->
                 worlds.add(infoFromFile(file))
                 );
    } catch (IOException e) {
      Log.d(LOG_TAG, "Failed finding game files: " + e);
    }
    out.print(formatter.worlds(worlds));
  }
  
  private void newWorld(HttpServletRequest request, HttpServletResponse response, String world, String format) {
      String addr = request.getRemoteAddr();
      String id = addr + "-" + (counter++) + "-" + System.currentTimeMillis();
      try {
        engine = engineStore.newEngine(id, world);
        String site = new String("/lifegame?gameId=" + id + "&format=" + format);
        response.setStatus(response.SC_MOVED_TEMPORARILY);
        response.setHeader("Location", site);
        info(request, "new game: gameId=" + id);
        return;
      } catch (EngineStore.EngineStoreException e) {
        debug(request, "failed creating new world: " + e);
        // TODO: really, I mean really. Handle this!
      }
  }

  private void admin(HttpServletRequest request, PrintWriter out, Formatter formatter) {
    EngineStore store = EngineStore.getInstance();
    debug(request, "admin requested");
    //    out.print(formatter.start());
    out.print(formatter.games(store));
    //out.print(formatter.end());
  }

  private void exit(HttpServletRequest request, HttpServletResponse response,
                    PrintWriter out, Formatter formatter, String gameId) {
    if (gameId==null) {
      debug(request, "Invalid gameId: " + gameId);
      return;
    }
    if (! correctClientAddress(request, gameId)) {
      debug(request, "exit, bad ip");
    } else {
      EngineStore store = EngineStore.getInstance();
      store.removeEngine(gameId);
      info(request, "exit game: gameId=" + gameId);
    }
    
    String site = new String("/");
    response.setStatus(response.SC_MOVED_TEMPORARILY);
    response.setHeader("Location", site);
  }

  private void handleGame(HttpServletRequest request, HttpServletResponse response,
                          PrintWriter out,
                          Formatter formatter,
                          String gameId,
                          String suggestion,
                          String actionThing,
                          String dropThing) throws IOException{
    try {

      debug(request, "handle id:    " + gameId);

      if (! correctClientAddress(request, gameId)) {
        debug(request, "bad ip: " + request.getRemoteAddr());
        out.print(formatter.error("IP address differs from originating"));
        return;
      }
      
      engine = engineStore.engine(gameId);
      //      System.out.println("handleGame() engine:" + engine);

      if (engine==null) {
        out.print(formatter.invalidGameId());
        return;
      }

      engineStore.updateTimeStamp(gameId);
      
      if (suggestion!=null) {
        debug(request, "suggestion:    \"" + suggestion + "\"");
        engine.handleExit(URLDecoder.decode(suggestion, "UTF-8"));  
      } else if (actionThing!=null) {
        String thing = URLDecoder.decode(actionThing, "UTF-8");
        for (ThingAction action: engine.situation().actions()) {
          if (action.thing().equals(thing)) {
            engine.addActionThing(action);
            break;
          }
        }
      } else if (dropThing!=null) {
        String thing = URLDecoder.decode(dropThing, "UTF-8");
        for (Map.Entry<ThingAction, Integer> entry : engine.things().entrySet()) {
          ThingAction tmpThing = entry.getKey();
          if (tmpThing.thing().equals(dropThing)) {
            engine.removeActionThing(tmpThing);
            break;
          }
        }
      }
      writeSituation(request, response, out,
                     formatter, gameId);
      /*
      Situation here = engine.situation();
      
      if (engine.gameOver()) {
        EngineStore.getInstance().removeEngine(gameId);
        out.print(formatter.win());
      } else {
        out.print(formatter.situation(engine.gameTitle(),
                                      engine.gameSubTitle(),
                                      here.title(),
                                      engine.explanation(),
                                      here.description(),
                                      here.suggestions(),
                                      engine.things(),
                                      here.actions()));
        */
        /*
          out.print(formatter.title(here.title()));
          out.print(formatter.explanation(engine.explanation()));
          out.print(formatter.description(here.description()));
          out.print(formatter.suggestions(here.suggestions()));
          out.print(formatter.actions(here.actions()));
          out.print(formatter.things(engine.things()));
        */
      /*        if (debug) {
          out.print(formatter.debug("[score:" + engine.score() + " | " +
                                   "situations: " + engine.situationCount() + 
                                   "]"));
                                   }*/
      /*}*/
      
      // TODO: accept input frmo user instead ;)
      /*      ThingAction thing = engine.situation().actions().get(0);
              if (thing!=null) {
              engine.addActionThing(thing);
              }
      */
      
    } catch (EngineStore.EngineStoreException e) {
      debug(request, "" + e);
      out.print(e);
    }
  }

  void writeSituation(HttpServletRequest request,
                      HttpServletResponse response,
                      PrintWriter out,
                      Formatter formatter,
                      String gameId) {
    Situation here = engine.situation();
      
    if (engine.gameOver()) {
      EngineStore.getInstance().removeEngine(gameId);
      out.print(formatter.win());
    } else {
      out.print(formatter.situation(engine.gameTitle(),
                                    engine.gameSubTitle(),
                                    here.title(),
                                    engine.explanation(),
                                    here.description(),
                                    here.suggestions(),
                                    engine.things(),
                                    here.actions()));
        if (debug) {
          out.print(formatter.debug("[score:" + engine.score() + " | " +
                                   "situations: " + engine.situationCount() + 
                                    "]"));
        }
    }
  }
  
}

