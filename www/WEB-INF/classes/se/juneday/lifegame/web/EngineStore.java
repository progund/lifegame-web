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
import se.juneday.lifegame.web.format.Formatter;
import se.juneday.lifegame.web.format.HTMLFormatter;
import se.juneday.lifegame.web.format.JSONFormatter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Collections;
import java.util.HashMap;
import java.time.Instant;
import java.time.Duration;

public class EngineStore  {

  public final String file_prefix = "www/WEB-INF/data/";
  public static final String LOG_TAG = EngineStore.class.getCanonicalName();

  // TODO: read from resource instead
  private final static int MAX_ENGINES = 200;
  private static int MINUTE = 1000 * 60;
  private static int HOUR = 60 * MINUTE;
  public final static int MAX_ENGINE_AGE = HOUR * 48  ; 

  public class EngineStoreModel {
    public LifeGameEngine engine;
    public Instant lastUse;
    public String nick;
    EngineStoreModel(LifeGameEngine engine) {
       this.engine=engine;
       this.lastUse=Instant.now();
    }
    EngineStoreModel(LifeGameEngine engine, String nick) {
       this.engine=engine;
       this.lastUse=Instant.now();
       this.nick=nick;
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
  
  private LifeGameEngine newEngine(String world) {
    LifeGameEngine engine;
    try { 
      engine = new LifeGameEngine(file_prefix + "/" + world + ".json" );
    } catch (InvalidLifeException e) {
      Log.e(LOG_TAG, "Failed creating game");
      return null;
    }
    return engine;
  }


  public LifeGameEngine newEngine(String gameId, String world, String nick)
    throws EngineStoreException {
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
        engines.put(gameId, new EngineStoreModel(engine, nick));
      } else {
        engine = engines.get(gameId).engine;
      }
      Log.e(LOG_TAG, "newEngine() engines added: " + gameId);
    }
    return engine;
  }

  public void removeEngine(String gameId) {
    synchronized(engines){
      engines.remove(gameId);
      Log.e(LOG_TAG, "removeOldEngines() engines removed: " + gameId);
    }
  }
  
  public long minutesLeft(String gameId) {
    return millisLeft(gameId)/1000/60 ;
  }
  
  public long millisLeft(String gameId) {
    Duration res = Duration.between(engines.get(gameId).lastUse, Instant.now());
    return (MAX_ENGINE_AGE - res.toMillis()) ;
  }
  
  public String nick(String gameId) {
    return engines.get(gameId).nick;
  }
  
  public static class EngineStoreException extends Exception {
    public EngineStoreException(String message) {
      super(message);
    }
    public EngineStoreException(String message, Exception cause) {
      super(message, cause);
    }
  }

  public void removeOldEngines() {
    synchronized(engines){
      for (Map.Entry<String, EngineStoreModel> entry : engines.entrySet()) {
        Duration res = Duration.between(entry.getValue().lastUse, Instant.now());
        
        if (res.toMillis() > MAX_ENGINE_AGE) {
          engines.remove(entry.getKey());
          Log.e(LOG_TAG, "removeOldEngines() engines removed: " + entry.getKey());
        }
      }
    }
  }
  
}

