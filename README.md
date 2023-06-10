# Scrabble

Code for our Java class's final project.

Similar to Words with Friends, an online Scrabble-inspired game.

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
    * `tiles: char[]` - initial tiles given to the player, comma seperated
    * `your_turn: boolean` - whether the client who recieves this message goes first
  * Sent from server
* `random_cancel` - remove oneself from the random game queue if they are waiting
  * If the user is not in the queue, this will be ignored by the server
* `turn:{pts}:{tiles}` - request to submit a turn
  * Payload
    * `pts: int` - points the word is worth
    * `tiles: (char, int, int)[]` - new tiles added to the board in letter, row, column format colon seperated
  * Responses
    * `turn_success:{tiles}` - turn was successfully validated
      * Payload
        * `tiles: char[]` - new tiles taken from the "tile bag", comma seperated
    * `turn_fail:{reason}` - turn was not validated
      * `reason: String` - reason for invalid turn
* `opponent_turn:{pts}:{tiles}` - opponent played a turn
  * This is just an echo of the `turn` message sent by the other player. Payload is the same and there is no response that should be sent after this messaage. Clients should note that it is their turn.
* `leave` - forfeit the current game, giving the other player the win
  * No responses, the equivalent to this is sent when the client's connection is closed (they close the app)
* `opponent_left` - the opponent left the game
  * This is sent when the other player sends a `leave` message. No response.


If the client sent the server a malformed request (e.g. `radnom` instead of `random`)
or a request not in this documentation (e.g. `abc123`),
the server will respond with `msg_fail:unkown request`.
