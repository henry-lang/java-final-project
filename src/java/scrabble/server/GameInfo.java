package scrabble.server;

public class GameInfo {
    public String id;
    public boolean maxPlayers;

    public GameInfo(String id) {
        this.id = id;
        this.maxPlayers = false;
    }
}
