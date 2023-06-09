package scrabble.server;

// Info about a connected client
public class ClientInfo {
    // The current state of the client
    public ClientState state;

    // The gameID that the client is in, if applicable
    public String gameID;

    // The username of the player, if it has been set
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