package se.juneday.lifegame.web.storage;

public class Game {
  private String gameId;
  private int situationCount;
  private int score;
  private String nick;

  public Game(String gameId, int situationCount, 
              int count,  String nick) {
    this.gameId = gameId;
    this.situationCount = situationCount;
    this.score = score;
    this.nick = nick;
  }

  public String gameId() {
    return gameId;
  }

  public int situationCount() {
    return situationCount;
  }

  public int score() {
    return score;
  }

  public String nick() {
    return nick;
  }

  @Override
  public String toString() {
    return gameId + " / " + nick + " / " + situationCount + " / " + score;
  }

}
