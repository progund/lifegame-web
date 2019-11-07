package se.juneday.lifegame.web;

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
import java.util.HashMap;

public class EngineStore  {

  public final String file = "www/WEB-INF/data/univ-game-swe.json";

  // TODO: read from resource instead
  private final static int MAX_ENGINES = 2;

  private Map<String, LifeGameEngine> engines;

  private static EngineStore instance;
  
  public static EngineStore getInstance() {
    if (instance==null) {
      instance = new EngineStore();
    }
    return instance;
  }

  private EngineStore() {
    engines = new HashMap<String, LifeGameEngine>();
  }

  private LifeGameEngine newEngine() {
    LifeGameEngine engine;
    try { 
      engine = new LifeGameEngine(file);
    } catch (InvalidLifeException e) {
      System.out.print("Failed creating game");
      return null;
    }
    
    try { 
      LifeVerifier verifier = new LifeVerifier(file);
      verifier.verify();
      System.out.println("Verification report");
      System.out.println(" * Failures: " + verifier.failures());
      System.out.println(" * Missing situations: " + verifier.missingSituations());
      System.out.println(" * Missing things:     " + verifier.missingThings());
      System.out.println(" * Missing exits:      " + verifier.missingExits());
      if (verifier.failures()!=0) {
        //System.exit(1);
      }
    } catch (LifeVerifierException | InvalidLifeException e) {
      System.out.print("Failed verifying game");
    }
    return engine;
  }

  public LifeGameEngine engine(String gameId) throws EngineStoreException {
    if (gameId==null) {
      return null;
    }
    LifeGameEngine engine = engines.get(gameId);
    if (engine==null) {
      if (engines.size() >= MAX_ENGINES ) {
        throw new EngineStoreException("Too many engines created");
      }
      engine = newEngine();
      engines.put(gameId, engine);
    }
    return engine;
  }
        
  public static class EngineStoreException extends Exception {
    public EngineStoreException(String message) {
      super(message);
    }
    public EngineStoreException(String message, Exception cause) {
      super(message, cause);
    }
  }
  
}

