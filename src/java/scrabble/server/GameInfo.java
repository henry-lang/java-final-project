package scrabble.server;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;

public class GameInfo {
    // All the letters in the bag, these correspond to the frequencies in the BAG_FREQUENCIES array
    private static final char[] BAG_LETTERS = {'e', 'a', 'i', 'o', 't', 'r', 's', 'd', 'n', 'l', 'u', 'h', 'g', 'y', 'b', 'c', 'f', 'm', 'p', 'w', 'v', 'k', 'x', 'j', 'q', 'z'};

    // The frequencies of each letter in the bag
    private static final int[] BAG_FREQUENCIES = {13, 9, 8, 8, 7, 6, 5, 5, 5, 4, 4, 4, 3, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1};

    // The id of the game
    public String id;

    // Player 1's connection
    public SocketChannel playerOne;

    // Player 2's connection
    public SocketChannel playerTwo;

    // The player whose turn it is
    public int turn = 1;

    // Whether the game is filled up
    public boolean maxPlayers;

    // The amount of players connected to the game currently
    private int players;

    // The tiles still in the tile bag
    public final ArrayList<Character> tileBag = new ArrayList<>();

    public GameInfo(String id, SocketChannel playerOne, SocketChannel playerTwo) {
        this.id = id;
        this.maxPlayers = false;
        this.playerOne = playerOne;
        this.playerTwo = playerTwo;

        // Add the initial tiles to the bag
        for(int i = 0; i < BAG_LETTERS.length; i++) {
            for(int j = 0; j < BAG_FREQUENCIES[i]; j++) {
                tileBag.add(BAG_LETTERS[i]);
            }
        }

        // Shuffle the bag to add randomness
        Collections.shuffle(tileBag);
    }

    public GameInfo(String id) {
        this(id, null, null);
    }

    // Add a player to the game
    public void addPlayer(SocketChannel player) {
        players++;
        if(players == 1) {
            playerOne = player;
        } else {
            playerTwo = player;
        }
        if (players == 2) maxPlayers = true;
    }

    // Remove a player from the game
    public void removePlayer() {
        players--;
        if (players <= 2) maxPlayers = false;
    }

    // Get message to send to the client with new tiles from the bag
    public String getTileMessage(int maxTiles) {
        if(tileBag.size() == 0) {
            return "_";
        } else {
            StringBuilder msg = new StringBuilder();
            int given = 0;
            while(given < maxTiles && tileBag.size() > 0) {
                msg.append(getTile());
                if(given < maxTiles - 1) {
                    msg.append(',');
                }
                given++;
            }

            return msg.toString();
        }
    }

    // Get the opponent of a certain player in the game
    public SocketChannel getOpponent(SocketChannel client) {
        if(client == playerOne) {
            return playerTwo;
        } else if(client == playerTwo) {
            return playerOne;
        } else {
            return null;
        }
    }

    // Get a tile from the tile bag
    public char getTile() {
        return tileBag.remove(tileBag.size() - 1);
    }
}
