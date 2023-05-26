package scrabble.server;

public class GameInfo {
    public String id;
    public boolean maxPlayers;
    private int players;

    public GameInfo(String id) {
        this.id = id;
        this.maxPlayers = false;
    }

    public void addPlayer() {
        players++;
        if (players == 2) maxPlayers = true;
    }

    public void removePlayer() {
        players--;
        if (players <= 2) maxPlayers = false;
    }
}
