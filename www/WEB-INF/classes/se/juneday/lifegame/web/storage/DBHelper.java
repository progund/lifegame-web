package se.juneday.lifegame.web.storage;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBHelper {

  // Tables
  private static final String GAME_TABLE = "games";

  // Product table columns
  public static final String GAME_ID = "id";
  public static final String SITUATION_COUNT = "situationcount";
  public static final String SCORE = "score";
  public static final String NICK = "nick";

  private static final String DB_URL =
    "jdbc:sqlite:www/WEB-INF/db/lifegame.db";

  private static Connection connection;
  static {
    try {
      connection = DriverManager.getConnection(DB_URL);
    } catch (SQLException sqle) {
      System.err.println("Couldn't get connection to " + DB_URL +
                         sqle.getMessage());
    }
  }

  public static void storeGame(Game g) {
    try {
    Statement statement = connection.createStatement();
    statement.executeUpdate("INSERT INTO Games " + 
                            "VALUES (" +
                            "\"" + g.gameId() + "\", " +
                            + g.situationCount() + ", " +
                            + g.score() + ", " +
                            "\"" + g.nick() + "\")"); 
    } catch (SQLException sqle) {
      System.err.println("Couldn't store game: " + sqle.getMessage());
    }
  }
  
  public static ResultSet gamesResultSet() {
    try {
      Statement statement = connection.createStatement();
      StringBuilder SQL = new StringBuilder("SELECT ")
        .append(GAME_ID).append(", ")
        .append(SITUATION_COUNT).append(", ")
        .append(SCORE).append(", ")
        .append(NICK).append(" ")
        .append(" FROM ").append(GAME_TABLE)
        .append(";");
      System.out.println("SQL: " + SQL);
      return statement.executeQuery(SQL.toString());
    } catch (SQLException sqle) {
      System.err.println("Couldn't get resultset with products: " + sqle.getMessage());
      return null;
    }
  }
  
}
