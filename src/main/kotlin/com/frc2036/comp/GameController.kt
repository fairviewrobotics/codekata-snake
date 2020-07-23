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

    private val game = Game(System.getenv()["SNAKE_OBSERVE_KEY"] ?: "observe0", System.getenv()["SNAKE_ADMIN_KEY"] ?: "admin0", System.getenv("SNAKE_NO_DEFAULT_KEYS") == null)

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

    @RequestMapping(value = ["/reset"], method = [RequestMethod.POST], produces = ["application/json"])
    @Synchronized
    fun postReset(@RequestParam key: String): String {
        return game.apiReset(key)
    }

    @RequestMapping(value = ["/soft_reset"], method = [RequestMethod.POST], produces = ["application/json"])
    @Synchronized
    fun postSoftReset(@RequestParam key: String): String {
        return game.apiSoftReset(key)
    }

    @RequestMapping(value = ["/set_player"], method = [RequestMethod.POST], produces = ["application/json"])
    @Synchronized
    fun postPlayer(@RequestParam key: String, @RequestParam index: Int, @RequestParam name: String, @RequestParam playerKey: String): String {
        return game.apiSetPlayer(key, index, name, playerKey)
    }

    @RequestMapping(value = ["/set_config"], method = [RequestMethod.POST], produces = ["application/json"])
    @Synchronized
    fun postConfig(@RequestParam key: String, @RequestParam turnsForFood: Int, @RequestParam turnsForWin: Int): String {
        return game.apiSetConfig(key, turnsForFood, turnsForWin)
    }
}