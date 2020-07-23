# Codekata-Snake

## Game 
Codekata-Snake is played by 4 players on a 25x25 board.

Each player controls a snake (a chain of cells, with one cell being the head, and the rest forming a body that follows the head).

### Gameplay
Every turn, each player chooses how to move the head of their snake (either left, up, right, or down).

Moves are applied at the same time, once all players have choosen their move.

One cell on the board contains food. If a player moves onto it (eats it), the length of their snake increases by one. Once the food is eaten, a new food is placed on a different cell.

If a player moves into a cell already occupied (including by themselves), they die. If two players move into the same cell, they both die. If a players moves beyond the edges of the board, they die. Dead players do not move, but their cells remain on the board.

#### Winning
The last player left alive wins. If all remaining players die on the same turn, the player with the longest length wins. If multiple snakes share the same length, the player closest to the current position of the food (by manhattan distance) wins. If multiple players are equally close to the food, the lowest numbered player wins.

### Starting Positions
Each player's snake starts with a length of 1.

- Player 0 starts at 0,0
- Player 1 starts at 24,0
- Player 2 starts at 0,24
- Player 3 starts at 24,24

The food starts at 12,12. Once it is eaten, it will be moved to a new cell.

## Game API

Each player is given a unique key to make api requests with. For the default server configuration, the keys are:
`key0`, `key1`, `key2`, `key3`

For a real competition, tokens will be distributed beforehand.

### Routes

All responses returned by the api are valid JSON.

#### `GET /api/board - params(key: String)`
Returns the state of the game, in the following format:
```json
{ 
"board":
    [
        [0,  -1, -1, -1, ...],
        [-1, -1, -1, -1, ...],
        [-1, -1, -1, -1, ...],
        ...
    ],
"heads":
    [[0, 0], [0, 24], [24, 0], [24, 24]],
"food":
    [12, 12]
}
```

The `food` field of the response indicates the location of the food (`[x, y]`).

The `heads` field of the response indicates the location of each player's head. Your head location is at index 0, and your opponents are at indicies 1, 2, and 3. (`[[x, y], [x, y], ...]`).

The `board` field of the response indicates the state of each cell. It is indexed as `[x][y]`. For each cell, `-1` indicates it is not occupied, `0` indicates it is occupied by you, and `1`, `2`, and `3` indicate it is occupied by an opponent. Food is not marked in the `board` field. Use the `food` field (documented above) to figure out where food is located.

#### `GET /api/move_needed - params(key: String)` 
Returns `true` if the player needs to make a move on the current board, or `false` if they don't need to make a move (either they are waiting on other players, or are dead).

#### `POST /api/move - params(key: String, move: Int)`
Make a move.
- `0` - left
- `1` - up
- `2` - right
- `3` - down

## Running The Server
The server can be run with `./gradlew bootRun`. It serves by default on `http://localhost:8080`.

## Observation API

Players should NOT use these api routes. They should only be used if you are writing a client to show the state of the game. In most cases, the provided observation client should be sufficient.

There is a special observation key. By default, it is `observe0`. In real competition, it will be something else.

#### `GET /api/progress - params(key: String)`
```json
{
  "dead": [false, true, false, true],
  "moved": [false, true, false, true],
  "lengths": [1, 1, 1, 1],
  "winner": -1,
  "names": ["Player 0", "Player 1", "Player 2", "Player 3"],
  "turn": 0
}
```
`dead` indicates which players have died. `moved` indicates which players have sent in their moves for the current turn. `lengths` indicates the current length of the snakes. `winner` indicates who has won the game, or `-1` if no player has won. `names` indicates the names of the player. `turn` indicates the current turn number.

If the observation key is used on `/api/board`, the state of the board will be returned, with indexes corresponding to player numbers.

## Game Admin API

Players should NOT use these api routes. For testing the default setup should work fine. If you are running a real competition, you should use this api.

There is a special admin key. By default, it is `admin0`. In real competition, it will be something else.

#### `POST/api/reset - params(key: String)`
Resets the state of the game, and disables players api keys (they will need to be set with `/api/set_player`).

#### `POST /api/set_player - params(key: String, index: Int, name: String, playerKey: String)`
Set the name and key for the given player.

### Environment Variable Config
The server uses the following environment variables to modify its behavior:
- `SNAKE_OBSERVE_KEY`: set the observation key. If not set, defaults to `observe0`.
- `SNAKE_ADMIN_KEY`: sets the admin key. If not set, defaults to `admin0`.
- `SNAKE_NO_DEFAULT_KEYS`: if set, player keys will be disabled until `/api/set_player` calls are made. If not set, player keys are `key0`, `key1`, `key2`, and `key3`

## Credits
- Implementation: Edward Wawrzynek
- Thanks to Saurabh Totey & Elia Gorokhovsky for [the original implementation](https://github.com/FHSCodeClub/Code-Kata-Snek)