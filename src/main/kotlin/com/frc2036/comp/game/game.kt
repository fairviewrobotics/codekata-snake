package com.frc2036.comp.game

import kotlin.math.abs
import kotlin.math.min
import kotlin.random.Random

/* TODO: refactor players as structures separate from the game instance */

class Game(val observeKey: String, val adminKey: String, val defaultPlayerKeys: Boolean) {
    /* empty player keys are invalid */
    val keys = if(defaultPlayerKeys) mutableListOf("key0", "key1", "key2", "key3") else mutableListOf("", "", "", "")

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

    var turns = 0
    var winner = -1

    var turnsFoodNotEaten = 0

    var TURNS_TO_MOVE_FOOD = 60
    var TURNS_TO_FORCE_END = 4096

    // names of each player
    val names = mutableListOf("Player 0", "Player 1", "Player 2", "Player 3")

    // get player index for key
    fun playerFromKey(key: String): Int? {
        if(key == "") return null

        val index = keys.indexOf(key)
        return if(index == -1) null else index
    }

    // reset state of the game
    fun reset() {
        food = Pair(12, 12)
        turnsFoodNotEaten = 0
        turns = 0
        winner = -1

        for(i in 0 until 4) {
            players[i] = arrayOf(mutableListOf(Pair(0, 0)), mutableListOf(Pair(24, 0)), mutableListOf(Pair(0, 24)), mutableListOf(Pair(24, 24)))[i]
            dead[i] = false
            moved[i] = false
            moves[i] = -1
        }
    }

    // reset api keys
    fun resetKeys() {
        for(i in 0 until 4) {
            keys[i] = ""
            names[i] = "Player $i"
        }
    }

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
        val player = playerFromKey(key) ?: -1
        if (player == -1 || dead[player] || moved[player]) return false

        moves[player] = move
        moved[player] = true

        if (allMovesMade()) doTurn()

        return true
    }

    // move food to a new location
    fun moveFood() {
        turnsFoodNotEaten = 0
        /* pick a random location that is not in a player and has at least distance 5 from all heads */
        val validLocs = mutableListOf<Pair<Int, Int>>()

        for(x in 0..24) {
            for(y in 0..24) {
                val occupied = players.any {player -> player.any {cell -> cell == Pair(x, y)}}
                if(occupied) continue

                val dists = players.map { player ->
                    abs(player[0].first - x) + abs(player[0].second - y)
                }

                if((dists.min() ?: 100) < 10) continue
                validLocs.add(Pair(x, y))
            }
        }

        food = if(validLocs.isNotEmpty()) validLocs.random() else Pair(0, 0)
    }

    // check for a winner, and set winner if somebody has won
    fun checkWinner() {
        if(winner != -1) return
        val alive = dead.count { d -> !d}

        if(alive == 0 || turns > TURNS_TO_FORCE_END) {
            val maxLen = players.map { p -> p.size }.max() ?: 0
            val numMaxLen = players.count { p -> p.size >= maxLen }

            if(numMaxLen == 1) {
                winner = players.indexOfFirst { p -> p.size >= maxLen }
            } else {
                val distsToFood = players.map { p -> abs(food.first - p[0].first) + abs(food.second - p[0].second) }
                val minDist = distsToFood.min() ?: 0
                val numMin = distsToFood.count { d -> d <= minDist}

                if(numMin == 1) {
                    winner = distsToFood.indexOfFirst { d -> d <= minDist }
                } else {
                    winner = 0
                }
            }
        } else if(alive == 1) {
            winner = dead.indexOfFirst { d -> !d }
            return
        }
    }

    // run a turn
    fun doTurn() {
        turnsFoodNotEaten++
        turns++
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
            if(!player.any { cell -> cell == newHeads[i]})
                player.add(0, newHeads[i])
        }

        if(doMoveFood) moveFood()
        if(turnsFoodNotEaten > TURNS_TO_MOVE_FOOD) {
            moveFood()
        }

        checkWinner()
        resetTurn()
    }

    // the /api/progress route
    fun apiProgress(key: String): String {
        if(key != observeKey) return "{\"error\": \"invalid key\"}"

        return "{" +
                "\"dead\": ${dead.map {d -> if(d) "true" else "false"}}, " +
                "\"moved\": ${moved.map {m -> if(m) "true" else "false"}}, " +
                "\"lengths\": ${players.map {player -> player.size}}, " +
                "\"winner\": $winner, " +
                "\"names\": ${names.map {n -> "\"$n\""}}, " +
                "\"turn\": $turns}"
    }

    fun apiMoveNeeded(key: String): String {
        val player = playerFromKey(key) ?: -1
        if(player == -1) return "false"

        return if(!moved[player] && !dead[player]) "true" else "false"
    }

    fun apiBoard(key: String): String {
        val player = if(key == observeKey) 0 else playerFromKey(key) ?: -1
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

    fun apiReset(key: String): String {
        if(key != adminKey) return "{\"error\": \"invalid key\"}"

        reset()
        resetKeys()
        return "{\"error\": null}"
    }

    fun apiSoftReset(key: String): String {
        if(key != adminKey) return "{\"error\": \"invalid key\"}"

        reset()
        return "{\"error\": null}"
    }

    fun apiSetPlayer(key: String, index: Int, name: String, playerKey: String): String {
        if(key != adminKey) return "{\"error\": \"invalid key\"}"
        if(index >= 4 || index < 0) return "{\"error\": \"invalid player index\"}"

        names[index] = name
        keys[index] = playerKey

        return "{\"error\": null}"
    }

    fun apiSetConfig(key: String, turnsForFood: Int, turnsForWin: Int): String {
        if(key != adminKey) return "{\"error\": \"invalid key\"}"
        
        TURNS_TO_MOVE_FOOD = turnsForFood
        TURNS_TO_FORCE_END = turnsForWin

        return "{\"error\": null}"
    }
}
