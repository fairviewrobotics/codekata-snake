package com.frc2036.comp.game

import kotlin.math.abs

/* TODO: refactor players as structures separate from the game instance */

class Game(val keys: List<String>, val observeKey: String) {
    init {
        assert(keys.size == 4)
    }

    // locations of each player, as an array of cells
    // index 0 is head
    val players = arrayOf(mutableListOf(Pair(0, 0)), mutableListOf(Pair(24, 0)), mutableListOf(Pair(0, 24)), mutableListOf(Pair(24, 24)))

    // location of the food
    var food = Pair(12, 12)

    // if players are dead
    val dead = arrayOf(false, false, false, false)

    // if players have made a move yet this turn
    val moved = arrayOf(false, false, false, false)

    // cached moves to be made by each player
    // 0 - left, 1 - up, 2 - right, 3 - down
    val moves = arrayOf(-1, -1, -1, -1)

    var turnsFoodNotEaten = 0

    // check if all player have moved
    fun allMovesMade(): Boolean {
        for (i in 0..3) {
            if (!dead[i] && !moved[i]) return false
        }

        return true
    }

    // reset state for new turn
    fun resetTurn() {
        for (i in 0..3) {
            moved[i] = false
            moves[i] = -1
        }
    }

    // cache a move by a player
    // returns false if the key is invalid or player dead, true otherwise
    fun registerMove(key: String, move: Int): Boolean {
        val player = keys.indexOf(key)
        if (player == -1 || dead[player] || moved[player]) return false

        moves[player] = move
        moved[player] = true

        if (allMovesMade()) doTurn()

        return true
    }

    // move food to a new location
    fun moveFood() {
        turnsFoodNotEaten = 0
        /* pick location with largest average distance away from any heads,
         * that is also not occupied by a player */
        var maxDist = 0
        var newLoc = Pair(0, 0)
        for(x in 0..24) {
            for(y in 0..24) {
                val occupied = players.any {player -> player.any {cell -> cell == Pair(x, y)}}
                if(occupied) continue

                val dist = players.sumBy { player ->
                    abs(player[0].first - x) + abs(player[0].second - y)
                }

                if(dist > maxDist) {
                    maxDist = dist
                    newLoc = Pair(x, y)
                }
            }
        }

        food = newLoc
    }

    // run a turn
    fun doTurn() {
        turnsFoodNotEaten++
        // calculate new head positions for each player
        val newHeads = players.mapIndexed {i, player ->
            val dir = moves[i]
            val xAdj = when(dir) {
                0 -> -1
                2 -> 1
                else -> 0
            }
            val yAdj = when(dir) {
                1 -> -1
                3 -> 1
                else -> 0
            }

            var newX = player[0].first + xAdj
            var newY = player[0].second + yAdj
            if(newX < 0 || newX >= 25 || newY < 0 || newY >= 25) {
                dead[i] = true
                newX = player[0].first
                newY = player[0].second
            }
            Pair(newX, newY)
        }

        // if player ate food, add to their length + move food
        var doMoveFood = false
        for(i in 0..3) {
            if(newHeads[i] == food) {
                players[i].add(Pair(0, 0))
                doMoveFood = true
                break
            }
        }

        // remove last cell from each player
        players.forEachIndexed { i, player ->
            if(!dead[i]) player.removeAt(player.size - 1)
        }

        // check each new head location for collision with existing player or other head
        newHeads.forEachIndexed { i, head ->
            if(!dead[i]) {
                // check for collision with existing player
                players.map { player ->
                    player.map { cell ->
                        if (cell == head) dead[i] = true
                    }
                }
                // check for collision with other head
                newHeads.mapIndexed { j, other ->
                    if (i != j && other == head) dead[i] = true
                }
            }
        }

        // add new head locations to players
        players.forEachIndexed { i, player ->
            player.add(0, newHeads[i])
        }

        if(doMoveFood) moveFood()
        if(turnsFoodNotEaten > 20) {
            moveFood()
        }

        resetTurn()
    }

    // the /api/progress route
    fun apiProgress(key: String): String {
        if(key != observeKey) return "{\"error\": \"invalid key\"}"

        return "{" +
                "\"dead\": ${dead.map {d -> if(d) "true" else "false"}}, " +
                "\"moved\": ${moved.map {m -> if(m) "true" else "false"}}}"
    }

    fun apiMoveNeeded(key: String): String {
        val player = keys.indexOf(key)
        if(player == -1) return "{\"error\": \"invalid key\"}"

        return if(!moved[player] && !dead[player]) "true" else "false"
    }

    fun apiBoard(key: String): String {
        val player = if(key == observeKey) 0 else keys.indexOf(key)
        if(player == -1) return "{\"error\": \"invalid key\"}"

        /* generate blank board */
        val board = Array(25) { Array(25) { -1 } }

        /* create reordered copy of players array from perspective of player */
        val adjPlayers = listOf(players[player]) + players.filterIndexed {i, _ -> i != player}

        /* mark player locations on board */
        adjPlayers.forEachIndexed {i, p ->
            p.forEach { cell ->
                board[cell.first][cell.second] = i
            }
        }

        return "{" +
                "\"board\": ${board.map {col -> col.map {cell -> cell}}}," +
                "\"food\": [${food.first}, ${food.second}]," +
                "\"heads\": ${adjPlayers.map {p -> "[${p[0].first}, ${p[0].second}]"}}" +
                "}"
    }
}
