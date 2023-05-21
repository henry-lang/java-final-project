package scrabble.server;

public class ClientInfo {
    public ClientState state;
    public String gameID;

    public ClientInfo(ClientState state, String gameID) {
        this.state = state;
        this.gameID = gameID;
    }

    public ClientInfo(ClientState state) {
        this.state = state;
    }
}