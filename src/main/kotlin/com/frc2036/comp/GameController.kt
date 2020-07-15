package com.frc2036.comp

import com.frc2036.comp.game.*
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestParam

import kotlin.random.Random

// Controller to manage api calls to run the tournament
@RestController
@RequestMapping(value = ["/api"])
class GameController {

    private val game = Game(listOf("key0", "key1", "key2", "key3"), "observe0")

    /* get the state of the game */
    @RequestMapping(value = ["/board"], method = [RequestMethod.GET], produces = ["application/json"])
    @Synchronized
    fun getBoard(@RequestParam key: String): String {
        return game.apiBoard(key)
    }

    @RequestMapping(value = ["/progress"], method = [RequestMethod.GET], produces = ["application/json"])
    @Synchronized
    fun getProgress(@RequestParam key: String): String {
        return game.apiProgress(key)
    }

    @RequestMapping(value = ["/move"], method = [RequestMethod.POST], produces = ["application/json"])
    @Synchronized
    fun doMove(@RequestParam key: String, @RequestParam move: Int): String {
        return if(!game.registerMove(key, move)) "{\"error\": \"invalid key/player is dead\"}"
        else "{\"error\": null}"
    }

    @RequestMapping(value = ["/move_needed"], method = [RequestMethod.GET], produces = ["application/json"])
    @Synchronized
    fun getMoveNeeded(@RequestParam key: String): String {
        return game.apiMoveNeeded(key)
    }
}