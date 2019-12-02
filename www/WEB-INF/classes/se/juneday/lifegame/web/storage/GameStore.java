package se.juneday.lifegame.web.storage;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;

public class GameStore {


  public List<Game> games() {
    List<Game> games = new ArrayList<>();
    
    try {
      ResultSet rs = DBHelper.gamesResultSet();
      
      while (rs.next()) {
        String gameId = rs.getString(DBHelper.GAME_ID);
        int sit = rs.getInt(DBHelper.SITUATION_COUNT);
        int score = rs.getInt(DBHelper.SCORE);
        String nick = rs.getString(DBHelper.NICK);
        games.add(new Game(gameId, sit, score, nick));
      }
    } catch (Throwable a) { /* ; */ }
    return games;
  }


  public void storeGame(Game g) {
    DBHelper.storeGame(g);
  }
}


