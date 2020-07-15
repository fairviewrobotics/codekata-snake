package com.frc2036.comp

import com.frc2036.comp.game.*
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestParam

import kotlin.random.Random

// Controller to manage api calls to run the tournament
@RestController
@RequestMapping(value=["/api"])
class GameController {

    /* get the state of the game */
    @RequestMapping(value=["/board"], method=[RequestMethod.GET], produces=["application/json"])
    @Synchronized
    fun getBoard(@RequestParam key: String): String {
        return ""
    }

}