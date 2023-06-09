# Phrases with Phriends
<div>
<img src="./photos/main_menu.png" alt="The main menu" width="300">
<img src="./photos/game.png" alt="The game" width="300">
</div>

Code for our Java class's final project.

Similar to Words with Friends, an online Scrabble-inspired game.

[Demo video](https://youtu.be/m3QJrVPxIQA)

# INFORMATION ON HOW TO RUN THE PROGRAM

Our canvas submission contains two .jar files with the IntelliJ compiled code bundled into a .jar package will all libraries and resources (the dictionary) embedded.
After unzipping the project zip or closing the GitHub repository, you can run `python3 test_all.py` from the root directory of the project to fire up a server and two clients instantly.
Or, you can start up a server first in one terminal window, and then clients in the other two by using the following commands.

Currently, the server address is hardcoded as `localhost` in the client's `Scrabble.java`, so if you would like to connect to a remote address you can change this `SERVER_HOST` constant to whatever you would like (with a recompilation afterward).
Please remember that if you're recompiling you must use IntelliJ 
Our canvas submission contains two .jar files with the IntelliJ compiled code bundled into a .jar package will all libraries and resources (the dictionary) embedded. After unzipping the project zip or closing the Github repository, you can run `python3 test_all.py` from the root directory of the project to fire up a server and two clients instantly. Or, you can start up a server first in one terminal window, and then clients in the other two by using the following commands. Currently, the server address is hardcoded as `localhost` in the client's `Scrabble.java`, so if you would like to connect to a remote address you can change this `SERVER_HOST` constant to whatever you would like (with a recompile afterwards).

### For the client:
`java -jar ./out/artifacts/phrases_client/phrases_client.jar`

### For the server:
`java -jar ./out/artifacts/phrases_server/phrases_server.jar`

## Implementation details

### Multiplayer communication

Clients and the server exchange data through a TCP socket connection.
Possible requests and responses are:

* `random:{username}` - request to join a random game
  * Payload: None
  * Responses
    * `random_waiting` - successfully placed in the queue
    * `random_game_start` - complex, see below
* `random_game_start:{opponent_username}:{tiles}:{your_turn}` - random game has been found, and an opponent has been matched with you. *this may be sent without prior `random_waiting` message*
  * Payload
    * `opponent_username: String` - points the word is worth
    * `tiles: char[]` - initial tiles given to the player, comma separated
    * `your_turn: boolean` - whether the client who receives this message goes first
  * Sent from server
* `random_cancel` - remove oneself from the random game queue if they are waiting
  * If the user is not in the queue, this will be ignored by the server
* `turn:{pts}:{tile_rack_empty}:{tiles}` - request to submit a turn
  * Payload
    * `pts: int` - points the word is worth
    * `tile_rack_empty: boolean` - whether the player's tile rack has no tiles in it, used for the server to check if it was the last move in the game
    * `tiles: (char, int, int)[]` - new tiles added to the board in letter, row, column format colon separated
  * Responses
    * `turn_success:{game_over}:{tiles}` - turn was successfully validated
      * Payload
        * `game_over: boolean` - was the last turn just played 
        * `tiles: char[]` - new tiles taken from the "tile bag", comma separated. If there are no tiles, an single underscore is put in place of the tiles.
    * `turn_fail:{reason}` - turn was not validated
      * `reason: String` - reason for invalid turn
* `opponent_turn:{pts}:{game_over}:{tiles}` - opponent played a turn
  * This is just an echo of the `turn` message sent by the other player. Payload is the same and there is no response that should be sent after this message. If `game_over` (`tile_rack_empty` and no more tiles in the bag) is true, the game is over. Clients should note that it is their turn.
* `leave` - forfeit the current game, giving the other player the win
  * No responses, the equivalent to this is sent when the client's connection is closed (they close the app)
* `opponent_left` - the opponent left the game
  * This is sent when the other player sends a `leave` message. No response.


If the client sent the server a malformed request (e.g. `radnom` instead of `random`)
or a request not in this documentation (e.g. `abc123`),
the server will respond with `msg_fail:unkown request`.
