package com.game.service;

import com.game.controller.PlayerOrder;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.repository.PlayersRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
@Transactional
public class PlayersService {
    final PlayersRepository repository;

    public PlayersService(PlayersRepository repository) {
        this.repository = repository;
    }

    public List<Player> getAllPlayers(String name, String title, Race race, Profession profession, Long after,
                                      Long before, Boolean banned, Integer minExperience, Integer maxExperience,
                                      Integer minLevel, Integer maxLevel, PlayerOrder order,
                                      Integer pageNumber, Integer pageSize) {
        List<Player> filterPlayers = filter(repository.findAll(), name, title, race, profession, after,
                before, banned, minExperience, maxExperience,
                minLevel, maxLevel);

        if (order != null) {

            List<Player> sortedPlayers = sort(filterPlayers, order, pageNumber, pageSize);
            return sortedPlayers;

        } else return filterPlayers;
    }

    public Integer getListSize(String name, String title, Race race, Profession profession, Long after,
                                      Long before, Boolean banned, Integer minExperience, Integer maxExperience,
                                      Integer minLevel, Integer maxLevel) {
        List<Player> filterPlayers = filter(repository.findAll(), name, title, race, profession, after,
                before, banned, minExperience, maxExperience,
                minLevel, maxLevel);

         return filterPlayers.size();
    }


    private List<Player> sort(List<Player> beforeSort, PlayerOrder order, Integer pageNumber, Integer pageSize) {

        List<Player> afterSort = new ArrayList<>();

        if (order.equals(PlayerOrder.ID)) beforeSort.sort(Comparator.comparing(Player::getId));
        if (order.equals(PlayerOrder.BIRTHDAY)) beforeSort.sort(Comparator.comparing(Player::getBirthday));
        if (order.equals(PlayerOrder.EXPERIENCE)) beforeSort.sort(Comparator.comparing(Player::getExperience));
        if (order.equals(PlayerOrder.LEVEL)) beforeSort.sort(Comparator.comparing(Player::getLevel));
        if (order.equals(PlayerOrder.NAME)) beforeSort.sort(Comparator.comparing(Player::getName));


        for (int i =  pageSize * pageNumber; i < pageSize * (pageNumber + 1); i++) {
            if (i < beforeSort.size())
                afterSort.add(beforeSort.get(i));
        }

        return afterSort;
    }

    private List<Player> filter(List<Player> beforeFilter, String name,
                                String title, Race race,
                                Profession profession, Long after,
                                Long before, Boolean banned,
                                Integer minExperience, Integer maxExperience,
                                Integer minLevel, Integer maxLevel) {

        List<Player> afterFilter = new ArrayList<>();

        Date afterDate = after != null ? new Date(after) : null,
                beforeDate = before != null ? new Date(before) : null;

        beforeFilter.forEach(player -> {
                    if ((name != null) && !(player.getName().contains(name))) return;
                    if ((title != null) && !(player.getTitle().contains(title))) return;
                    if ((race != null) && !(player.getRace() == race)) return;
                    if ((profession != null) && !(player.getProfession() == profession)) return;
                    if ((after != null) && (player.getBirthday().before(afterDate))) return;
                    if ((before != null) && (player.getBirthday().after(beforeDate))) return;
                    if ((banned != null) && !(player.isBanned() == banned)) return;
                    if ((minExperience != null) && (player.getExperience().compareTo(minExperience) <= 0)) return;
                    if ((maxExperience != null) && (player.getExperience().compareTo(maxExperience) >= 0)) return;
                    if ((minLevel != null) && (player.getLevel().compareTo(minLevel) <= 0)) return;
                    if ((maxLevel != null) && (player.getLevel().compareTo(maxLevel) >= 0)) return;
                    afterFilter.add(player);
                }
        );
        return afterFilter;
    }


    public Boolean checkPlayer(Player player){

        if (player.getName() == null || player.getName().length() > 12 || player.getName().isEmpty()) return false;
        if (player.getTitle() == null || player.getTitle().length() > 30) return false;
        if (player.getRace() == null) return false;
        if (player.getProfession() == null) return false;
        if (player.getBirthday() == null || 0 > player.getBirthday().getTime() ||
                player.getBirthday().after(new Date(1100, 0, 1)) ||
         player.getBirthday().before(new Date(100, 0, 1))) return false;
        if (player.isBanned() == null) player.setBanned(false);
        if (player.getExperience() == null || player.getExperience() > 10000000 || player.getExperience() < 0) return false;
        return true;
    }

    public Player createNewPlayer(Player player){
        player.setLevel(getLevel(player.getExperience()));
        player.setUntilNextLevel(getUntilNextLevel(player.getExperience(), player.getLevel()));
        Player player1 = repository.save(player);
        return  player1;
    }

    private Integer getLevel(Integer exp){
        exp = exp == null? 0: exp;
        return (int) (Math.sqrt((double) 2500 + 200 * exp) - 50) / 100;
    }

    private Integer getUntilNextLevel(Integer exp, Integer lvl){
        exp = exp == null? 0: exp;
            return 50 * (lvl + 1)*(lvl + 2) - exp;
    }

    public boolean isValid(String lineID){
        Long id;
        try {
            id = Long.parseLong(lineID);
        } catch (NumberFormatException numberFormatException){
            return false;
        }
        return id > 0;
    }

    public Player findPlayer(String lineID){

        Long id = Long.parseLong(lineID);
        Player player = repository.findById(id).orElse(null);
        return player;
    }

    public Player updatePlayer(Player updatePlayer, Player player) {

        if (updatePlayer != null) {
            String name = player.getName();
            if (name != null) updatePlayer.setName(name);

            String title = player.getTitle();
            if (title != null) updatePlayer.setTitle(title);

            Race race = player.getRace();
            if (race != null) updatePlayer.setRace(race);

            Profession profession = player.getProfession();
            if (profession != null) updatePlayer.setProfession(profession);

            if (player.getBirthday() != null) {
                if (0 < player.getBirthday().getTime() &&
                        player.getBirthday().before(new Date(1100, 0, 1)) &&
                        player.getBirthday().after(new Date(100, 0, 1)))
                    updatePlayer.setBirthday(player.getBirthday());
                else throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }

            if (player.isBanned() != null) updatePlayer.setBanned(player.isBanned());

            Integer experience = player.getExperience();
            if (experience != null){
                if (experience > 0 && experience < 10000000) {
                updatePlayer.setExperience(experience);
                updatePlayer.setLevel(getLevel(updatePlayer.getExperience()));
                updatePlayer.setUntilNextLevel(getUntilNextLevel(updatePlayer.getExperience(), updatePlayer.getLevel()));
            } else
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }
        }

        return repository.save(updatePlayer);
    }

    public void deletePlayer(Player player){
        repository.delete(player);
    }
}
