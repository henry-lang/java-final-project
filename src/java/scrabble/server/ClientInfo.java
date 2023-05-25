package scrabble.server;

public class ClientInfo {
    public ClientState state;
    public String gameID;

    public String username;

    public ClientInfo(ClientState state, String gameID) {
        this.state = state;
        this.gameID = gameID;
    }

    public ClientInfo(ClientState state) {
        this.state = state;
    }

    public void setUsername(String name) { username = name; }
}