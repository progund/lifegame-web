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
import se.juneday.lifegame.web.format.HTMLFormater;

import java.util.Map;
import java.util.ArrayList;
import java.util.Scanner;


public class GameWeb extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {

    request.setCharacterEncoding(UTF_8.name());
    response.setContentType("text/html;charset=" + UTF_8.name());
    response.setCharacterEncoding(UTF_8.name());

    String gameId = request.getParameter("gameId");
    String format = request.getParameter("format");
    String write = request.getParameter("write");
    int nrParameters = request.getParameterMap().entrySet().size();
    Formater formater = new HTMLFormater();
    PrintWriter out = response.getWriter();

    try {
      LifeGameEngine engine = EngineStore.getInstance().engine(gameId);
      
      Situation here = engine.situation();
      out.print("format: " + format);
      out.print("\n");
      out.print("title: " + formater.title(here.title()));
      out.print("\n");
      out.print("description: " + formater.description(here.description()));
      out.print("\n");
      out.print("suggestions: " + formater.suggestions(here.suggestions()));
      out.print("\n");
      out.print("things: " + formater.actions(here.actions()));
      out.print("\n");
      out.print("your things: " + formater.things(engine.things()));
      out.print("\n");
      
      engine.handleExit("Går in");
    } catch (EngineStore.EngineStoreException e) {
      out.print(e);
    }

    out.close();
    }
    
  }
  
