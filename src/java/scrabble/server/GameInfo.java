package scrabble.server;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;

public class GameInfo {
    private static final char[] BAG_LETTERS = {'e', 'a', 'i', 'o', 't', 'r', 's', 'd', 'n', 'l', 'u', 'h', 'g', 'y', 'b', 'c', 'f', 'm', 'p', 'w', 'v', 'k', 'x', 'j', 'q', 'z'};
    private static final int[] BAG_FREQUENCIES = {13, 9, 8, 8, 7, 6, 5, 5, 5, 4, 4, 4, 3, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1};

    public String id;

    public SocketChannel playerOne;
    public SocketChannel playerTwo;

    public int turn = 1;

    public boolean maxPlayers;
    private int players;

    private final ArrayList<Character> tileBag = new ArrayList<>();

    public GameInfo(String id, SocketChannel playerOne, SocketChannel playerTwo) {
        this.id = id;
        this.maxPlayers = false;
        this.playerOne = playerOne;
        this.playerTwo = playerTwo;

        for(int i = 0; i < BAG_LETTERS.length; i++) {
            for(int j = 0; j < BAG_FREQUENCIES[i]; j++) {
                tileBag.add(BAG_LETTERS[i]);
            }
        }
        Collections.shuffle(tileBag);
    }

    public GameInfo(String id) {
        this(id, null, null);
    }

    public void addPlayer(SocketChannel player) {
        players++;
        if(players == 1) {
            playerOne = player;
        } else {
            playerTwo = player;
        }
        if (players == 2) maxPlayers = true;
    }

    public void removePlayer() {
        players--;
        if (players <= 2) maxPlayers = false;
    }

    public String getTileMessage(int maxTiles) {
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

    public SocketChannel getOpponent(SocketChannel client) {
        if(client == playerOne) {
            return playerTwo;
        } else if(client == playerTwo) {
            return playerOne;
        } else {
            return null;
        }
    }

    public char getTile() {
        return tileBag.remove(tileBag.size() - 1);
    }
}
