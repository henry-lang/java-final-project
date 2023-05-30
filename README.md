# Scrabble

Code for our Java class's final project.

Similar to Words with Friends, an online Scrabble-inspired game.

## Implementation details

### Multiplayer communication

Clients and the server exchange data through a TCP socket connection.
Possible requests and responses are:

* `logon:{name}` - request to log on with a username
  * Payload
    * `name: String` - Name to be used in active games
  * Responses
    * `logon_success` - successful logon
    * `logon_fail:{reason}` - failed to log on
      * Payload
        * `reason: String` - reason for logon failure
* `create` - request to host a new game
  * Payload: None
  * Responses
    * `create_success:{id}` - successfully created a new game
      * Payload
        * `id: String` - game ID to be used in games
    * `create_fail:{reason}` - failed to create a new game
      * Payload
        * `reason: String` - reason for failure
* `random` - request to join a random game
  * Payload: None
  * Responses
    * `random_waiting` - successfully placed in the queue
    * `random_game_found` - client has been matched with another - THIS MAY BE SENT INSTANTLY WITHOUT WAITING, but if client must wait for a game `random_waiting` will be sent
    * `random_fail:{reason}` - failed adding player to queue
* `join:{id}` - request to join a new game
  * Payload
    * `id: String` - ID of game to join
  * Responses
    * `join_success` - **!! TODO**
    * `join_fail:{reason}` - **!! TODO**
* `turn:{pts}:{tiles}:{pos}` - request to submit a turn
  * Payload
    * `pts: int` - points the word is worth
    * `tiles: Tile[]` - new tiles added to the board
    * `pos: int[][]` - corresponding positions of new tiles
  * Responses
    * `turn_success:{tiles}` - turn was successfully validated
      * Payload
        * `tiles: Tile[]` - new tiles taken from the "tile bag"
    * `turn_fail:{reason}` - turn was not validated
      * `reason: String` - reason for invalid turn

Server messages that are not in response to requests are:

* `game_start` - broadcast to exit queue and begin game
* `game_end:{winner}`- broadcast to end game
  * Payload
    * `winner: String` - name of winning player

If the client sent the server a malformed request (e.g. `jion` instead of `join`)
or a request not in this documentation (e.g. `abc123`),
the server will respond with `msg_fail:unkown request`.
