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
import java.util.concurrent.ConcurrentHashMap;
import java.util.Collections;
import java.util.HashMap;
import java.time.Instant;
import java.time.Duration;

public class EngineStore  {

  public final String file_prefix = "www/WEB-INF/data/";

  // TODO: read from resource instead
  private final static int MAX_ENGINES = 200;
  private final static int MAX_ENGINE_AGE = 1000 * 60 * 60 ; // 60 minutes

  public class EngineStoreModel {
    public LifeGameEngine engine;
    public Instant lastUse;
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
    /*    Map<String, EngineStoreModel> map = new HashMap<>();
    engines = Collections.synchronizedMap(map);
    */

    engines = new java.util.concurrent.ConcurrentHashMap<String, EngineStoreModel>();
  }

  public Map<String, EngineStoreModel> engines() {
    return engines;
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
    if (gameId==null) {
      return null;
    }
    LifeGameEngine engine = null;
    
    synchronized(engines){
      EngineStoreModel model = engines.get(gameId);
      if (model!=null) {
        engine = model.engine;
      }
    }
    return engine;
  }

  public void updateTimeStamp(String gameId) throws EngineStoreException {
    if (gameId==null) {
      return ;
    }
    LifeGameEngine engine = null;
    
    synchronized(engines){
      EngineStoreModel model = engines.get(gameId);
      model.lastUse=Instant.now();
    }
  }

  public Instant instant(String gameId) throws EngineStoreException {
    if (gameId==null) {
      return null;
    }
    synchronized(engines){
      EngineStoreModel model = engines.get(gameId);
      if (model!=null) {
        return model.lastUse;
      }
    }
    return null;
  }
  
  public LifeGameEngine newEngine(String gameId, String world) throws EngineStoreException {
    if (gameId==null) {
      return null;
    }
    LifeGameEngine engine;

    removeOldEngines();

    synchronized(engines){
      EngineStoreModel model;
      model = engines.get(gameId);
      if (model==null) {
        if (engines.size() >= MAX_ENGINES ) {
          throw new EngineStoreException("Too many engines created");
        }
        engine = newEngine(world);
        engines.put(gameId, new EngineStoreModel(engine));
      } else {
        engine = engines.get(gameId).engine;
      }
      System.out.println("newEngine() engines added: " + gameId);
    }
    return engine;
  }

  public void removeEngine(String gameId) {
    synchronized(engines){
      engines.remove(gameId);
      System.out.println("removeOldEngines() engines removed: " + gameId);
    }
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
    synchronized(engines){
      for (Map.Entry<String, EngineStoreModel> entry : engines.entrySet()) {
        Duration res = Duration.between(entry.getValue().lastUse, Instant.now());
        
        if (res.toMillis() > MAX_ENGINE_AGE) {
          engines.remove(entry.getKey());
          System.out.println("removeOldEngines() engines removed: " + entry.getKey());
        }
      }
    }
  }
  
}

