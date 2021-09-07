package com.game.controller;

import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.service.PlayersService;
import org.springframework.aop.target.ThreadLocalTargetSourceStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import static org.springframework.http.ResponseEntity.status;


@RestController
@RequestMapping("/rest")
public class PlayerController {

    private PlayersService service;

    @Autowired
  public PlayerController(PlayersService service){
      this.service = service;
  }



    @GetMapping("/players")
    public List<Player> getPlayersList(@RequestParam(value = "name", required = false) String name,
                                       @RequestParam(value = "title", required = false) String title,
                                       @RequestParam(value = "race", required = false) Race race,
                                       @RequestParam(value = "profession", required = false)Profession profession,
                                       @RequestParam(value = "after", required = false) Long after,
                                       @RequestParam(value = "before", required = false) Long before,
                                       @RequestParam(value = "banned", required = false)Boolean banned,
                                       @RequestParam(value = "minExperience", required = false) Integer minExperience,
                                       @RequestParam(value = "maxExperience", required = false) Integer maxExperience,
                                       @RequestParam(value = "minLevel", required = false) Integer minLevel,
                                       @RequestParam(value = "maxLevel", required = false) Integer maxLevel,
                                       @RequestParam(defaultValue = "ID", value = "order") PlayerOrder order,
                                       @RequestParam(defaultValue = "0", value = "pageNumber") Integer pageNumber,
                                       @RequestParam(defaultValue = "3", value = "pageSize") Integer pageSize
    ) {
        List<Player> resultList = service.getAllPlayers(name, title, race, profession,
                after, before, banned, minExperience, maxExperience, minLevel, maxLevel, order, pageNumber, pageSize);

        return resultList;
    }

    @GetMapping("/players/count")
    public Integer getPlayersListSize(@RequestParam(value = "name", required = false) String name,
                                       @RequestParam(value = "title", required = false) String title,
                                       @RequestParam(value = "race", required = false) Race race,
                                       @RequestParam(value = "profession", required = false)Profession profession,
                                       @RequestParam(value = "after", required = false) Long after,
                                       @RequestParam(value = "before", required = false) Long before,
                                       @RequestParam(value = "banned", required = false)Boolean banned,
                                       @RequestParam(value = "minExperience", required = false) Integer minExperience,
                                      @RequestParam(value = "maxExperience", required = false) Integer maxExperience,
                                      @RequestParam(value = "minLevel", required = false) Integer minLevel,
                                      @RequestParam(value = "maxLevel", required = false) Integer maxLevel,
                                      @RequestParam(defaultValue = "ID", value = "order") PlayerOrder order,
                                      @RequestParam(defaultValue = "0", value = "pageNumber") Integer pageNumber,
                                      @RequestParam(defaultValue = "3", value = "pageSize") Integer pageSize
    ) {
        Integer count = service.getListSize(name, title, race, profession,
                after, before, banned, minExperience, maxExperience,
                minLevel, maxLevel);

        return count;
    }

    @PostMapping("/players")
    public ResponseEntity<Player> createNewPlayer(@RequestBody Player player) {

        if (service.checkPlayer(player))
            return new ResponseEntity<>(service.createNewPlayer(player), HttpStatus.OK);
        else
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/players/{id}")
    public ResponseEntity<Player> getPlayer(@PathVariable String id) {

        if (service.isValid(id)) {
            Player player = service.findPlayer(id);
            if (player != null)
                return new ResponseEntity<>(service.createNewPlayer(player), HttpStatus.OK);
            else
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/players/{id}")
    public ResponseEntity<Player> UpdatePlayer(@PathVariable String id, @RequestBody Player player) {

        if (service.isValid(id)) {
            Player findPlayer = service.findPlayer(id);
            if (findPlayer != null)
                return new ResponseEntity<>(service.updatePlayer(findPlayer, player), HttpStatus.OK);
            else
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @DeleteMapping("/players/{id}")
    public ResponseEntity<Player> deletePlayer(@PathVariable String id) {
        if (service.isValid(id)) {
            Player findPlayer = service.findPlayer(id);
            if (findPlayer != null) {
                service.deletePlayer(findPlayer);
                return new ResponseEntity<>(HttpStatus.OK);
            }
            else
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
}


