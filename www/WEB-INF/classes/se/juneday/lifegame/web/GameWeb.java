package se.juneday.lifegame.web;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Date;
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
import se.juneday.lifegame.web.format.Formater;
import se.juneday.lifegame.web.format.FormaterStore;

import java.util.Map;
import java.util.ArrayList;
import java.util.Scanner;
import java.time.Instant;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;


public class GameWeb extends HttpServlet {

  private static EngineStore engineStore;
  private LifeGameEngine engine;
  private static int counter = 1000;
  
  static {
    engineStore = EngineStore.getInstance();
  }

  
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {

    request.setCharacterEncoding(UTF_8.name());
    response.setContentType("text/html;charset=" + UTF_8.name());
    response.setCharacterEncoding(UTF_8.name());

    String gameId = request.getParameter("gameId");
    String format = request.getParameter("format");
    String suggestion = request.getParameter("suggestion");
    String actionThing = request.getParameter("pickup");
    String dropThing = request.getParameter("drop");
    String world = request.getParameter("world");

    if (format==null) {
      format = "html";
    }
    
    Formater formater = FormaterStore.getStore(format, gameId);
    PrintWriter out = response.getWriter();


    if (world!=null) {
      newWorld(request, response, world);
    } else {
      handleGame(request, response, out, formater, gameId, suggestion, actionThing, dropThing);
    }
    
    out.close();
  }

  private void newWorld(HttpServletRequest request, HttpServletResponse response, String world) {
      String addr = request.getRemoteAddr();
      String id = addr + "-" + (counter++) + "-" + System.currentTimeMillis();
      try {
        engine = engineStore.newEngine(id, world);
        String site = new String("/lifegame?gameId=" + id);
        response.setStatus(response.SC_MOVED_TEMPORARILY);
        response.setHeader("Location", site);
        return;
      } catch (EngineStore.EngineStoreException e) {
        // TODO: really, I mean really. Handle this!
      }
  }

  private void handleGame(HttpServletRequest request, HttpServletResponse response,
                          PrintWriter out,
                          Formater formater,
                          String gameId,
                          String suggestion,
                          String actionThing,
                          String dropThing) throws IOException{
    try {

      System.out.println("handleGame() id:    " + gameId);
      
      engine = engineStore.engine(gameId);
      //      System.out.println("handleGame() engine:" + engine);

      if (engine==null) {
        out.print(formater.invalidGameId());
        return;
      }
      
      if (suggestion!=null) {
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
      Situation here = engine.situation();
      
      if (here.title().equals(LifeGameEngine.WIN_SITUATION)) {
        EngineStore.getInstance().removeEngine(gameId);
        out.print(formater.start());
        out.print(formater.win());
        out.print(formater.end());
      } else {
        out.print(formater.start());
        out.print(formater.title(here.title()));
        out.print(formater.explanation(engine.explanation()));
        out.print(formater.description(here.description()));
        out.print(formater.suggestions(here.suggestions()));
        out.print(formater.actions(here.actions()));
        out.print(formater.things(engine.things()));
        out.print(formater.end());
      }
      
      // TODO: accept input frmo user instead ;)
      /*      ThingAction thing = engine.situation().actions().get(0);
      if (thing!=null) {
        engine.addActionThing(thing);
      }
      */
      
    } catch (EngineStore.EngineStoreException e) {
      out.print(e);
    }
  }
  
  
}

