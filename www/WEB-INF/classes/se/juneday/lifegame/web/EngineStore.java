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
import java.time.Instant;
import java.time.Duration;

public class EngineStore  {

  public final String file_prefix = "www/WEB-INF/data/";

  // TODO: read from resource instead
  private final static int MAX_ENGINES = 2;
  private final static int MAX_ENGINE_AGE = 60 * 60 * 1000; // 60 minutes

  private class EngineStoreModel {
    LifeGameEngine engine;
    Instant lastUse;
    EngineStoreModel(LifeGameEngine engine) {
       this.engine=engine;
       this.lastUse=Instant.now();
    }
  }
  
  private Map<String, EngineStoreModel> engines;

  private static EngineStore instance;
  
  public static EngineStore getInstance() {
    if (instance==null) {
      instance = new EngineStore();
    }
    return instance;
  }

  private EngineStore() {
    engines = new HashMap<String, EngineStoreModel>();
  }

  private LifeGameEngine newEngine(String world) {
    LifeGameEngine engine;
    try { 
      engine = new LifeGameEngine(file_prefix + "/" + world + ".json" );
    } catch (InvalidLifeException e) {
      System.out.print("Failed creating game");
      return null;
    }

    /*
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
      */
    return engine;
  }

  public LifeGameEngine engine(String gameId) throws EngineStoreException {
    removeOldEngines();
    if (gameId==null) {
      return null;
    }
    LifeGameEngine engine = null;
    
    EngineStoreModel model = engines.get(gameId);
    if (model!=null) {
      engine = model.engine;
    }
    return engine;
  }

  public LifeGameEngine newEngine(String gameId, String world) throws EngineStoreException {

    if (gameId==null) {
      return null;
    }
    LifeGameEngine engine;

    System.out.println("newEngine() " + gameId + " " + world);
    System.out.println("newEngine() engines count: " + engines.size());
    EngineStoreModel model = engines.get(gameId);
    if (model==null) {
      System.out.println("newEngine() model null");
      if (engines.size() >= MAX_ENGINES ) {
        throw new EngineStoreException("Too many engines created");
      }
      engine = newEngine(world);
      engines.put(gameId, new EngineStoreModel(engine));
      System.out.println("newEngine() added");
    } else {
      System.out.println("newEngine() model exists");
      engine = engines.get(gameId).engine;
    }
    System.out.println("newEngine() engines added: " + gameId);
    System.out.println("newEngine() engines count: " + engines.size());
    //    System.out.println("newEngine() engine: " + engines.get(gameId).engine);
    System.out.println("newEngine() engines added: " + gameId);
    System.out.println("newEngine() engines count: " + engines.size());
    return engine;
  }

  public void removeEngine(String gameId) {
    engines.remove(gameId);
  }
  
  public static class EngineStoreException extends Exception {
    public EngineStoreException(String message) {
      super(message);
    }
    public EngineStoreException(String message, Exception cause) {
      super(message, cause);
    }
  }


  private void removeOldEngines() {
    for (Map.Entry<String, EngineStoreModel> entry : engines.entrySet()) {
      Duration res = Duration.between(entry.getValue().lastUse, Instant.now());
      System.out.println(" ********************************** " + entry.getValue().lastUse + " " + Instant.now() + " ---> " + res.toMillis());

      if (res.toMillis() > MAX_ENGINE_AGE) {
        System.out.println(" ********************************** REMOVE " + entry.getKey());
        removeEngine(entry.getKey());
      }
      
    }
  }
  
}

