package com.codeoftheweb.salvo.controller;

import com.codeoftheweb.salvo.dto.SalvoDTO;
import com.codeoftheweb.salvo.dto.ShipDTO;
import com.codeoftheweb.salvo.entities.*;
import com.codeoftheweb.salvo.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class SalvoController{

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GamePlayerRepository gamePlayerRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private ShipRepository shipRepository;

    @Autowired
    private SalvoRepository salvoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder ;


    @RequestMapping("/games")
    public Map<String,Object> getAllGames(Authentication authentication){

        Map<String, Object> dto= new LinkedHashMap<>();

        dto.put("player", isGuest(authentication) ? "guest" : playerRepository.findByUserName(authentication.getName()).getDTO());
        dto.put("games",gameRepository.findAll().stream().map(game -> game.getDTO()).collect(Collectors.toList()));

        return dto;

    }

    private boolean isGuest(Authentication authentication) {

        return authentication == null || authentication instanceof AnonymousAuthenticationToken;

    }

    @RequestMapping("/game_view/{gamePlayerId}")
    public ResponseEntity<Object>  gameViewDTO(@PathVariable Long gamePlayerId, Authentication authentication) {

        GamePlayer gamePlayer= gamePlayerRepository.getOne(gamePlayerId);

        if(gamePlayer.getPlayer().getId()!=(playerRepository.findByUserName(authentication.getName()).getId())){

            return new ResponseEntity<>("This game does not belong to you", HttpStatus.UNAUTHORIZED);

        }

        Map<String,Object> dto = new LinkedHashMap<>();

        dto.put("gid",gamePlayer.getGame().getId());

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss");

        dto.put("created",formatter.format(gamePlayer.getGame().getCreationDate()));
        dto.put("gamePlayers", getAllGamePlayers(gamePlayer.getGame().getGamePlayers()));
        dto.put("ships",getAllShips(gamePlayer.getShips()));
        dto.put("salvos",getAllSalvoes(gamePlayer.getGame()));
        dto.put("scores",getAllScores(gamePlayer.getGame()));
        dto.put("hits", gamePlayer.getHitsDTO());

        return new ResponseEntity<>(dto, HttpStatus.ACCEPTED);

    }



    private List<Map<String,Object>> getAllGamePlayers(Set<GamePlayer> gamePlayers) {

        return gamePlayers.stream().map(gamePlayer -> gamePlayer.getDTO()).collect(Collectors.toList());

    }

    private Object getAllScores(Game game) {

        return game.getScores().stream().map(score -> score.getDTO()).collect(Collectors.toList());

    }

    private Object getAllSalvoes(Game game) {

        return game.getGamePlayers().stream().flatMap(gp->gp.getSalvos().stream().map(salvo-> salvo.getDTO())).collect(Collectors.toList());

    }

    private Object getAllShips(Set<Ship> ships) {

        return ships.stream().map(ship -> ship.getDTO()).collect(Collectors.toList());

    }

    @RequestMapping("/leaderboard")
    public List<Map<String,Object>> makeLeaderboard(){

        return playerRepository.findAll().stream().map(player -> playerLeaderboardDTO(player)).collect(Collectors.toList());

    }

    private Map<String,Object> playerLeaderboardDTO(Player player) {

        Map<String,Object> dto= new LinkedHashMap<>();

        dto.put("userName",player.getUserName());
        dto.put("scores", getScoreList(player));

        return dto;

    }

    private Map<String,Object> getScoreList(Player player) {

        Map<String,Object> dto= new LinkedHashMap<>();

        dto.put("total",player.getTotalScore());
        dto.put("won", player.getWins());
        dto.put("tied",player.getTied());
        dto.put("lost", player.getLooses());

        return dto;

    }

    @RequestMapping(path = "/players", method = RequestMethod.POST)
    public ResponseEntity<String> createPlayer(@RequestParam String username, @RequestParam String password) {

        if (username == null || password == null ){

            return new ResponseEntity<>("Name or password invalid", HttpStatus.BAD_REQUEST);

        }

        if (playerRepository.findByUserName(username)!=null){

            return new ResponseEntity<>("Name already used", HttpStatus.CONFLICT);

        }

        playerRepository.save(new Player(username, passwordEncoder.encode(password)));

        return new ResponseEntity<>("User has been created",HttpStatus.ACCEPTED);

    }

    @RequestMapping(path = "/games", method = RequestMethod.POST)
    public ResponseEntity<Object> createGame(Authentication authentication) {

        if(isGuest(authentication)){

            return new ResponseEntity<>("You must login to create game",HttpStatus.UNAUTHORIZED);

        }

        Game game = new Game();

        gameRepository.save(game);

        GamePlayer gamePlayer= new GamePlayer(game, playerRepository.findByUserName(authentication.getName()));

        gamePlayerRepository.save(gamePlayer);

        Map<String,Object> dto= new LinkedHashMap<>();

        dto.put("gpid", gamePlayer.getId());

        return new ResponseEntity<>(dto, HttpStatus.ACCEPTED);

    }

    @RequestMapping(path = "/game/{gameID}/players", method = RequestMethod.POST)
    public ResponseEntity<Object> joinGame(@PathVariable Long gameID, Authentication authentication) {

        if(isGuest(authentication)){

            return new ResponseEntity<>("You must login to join game",HttpStatus.UNAUTHORIZED);

        }

        if(gameRepository.findById(gameID).orElse(null)==null){

            return new ResponseEntity<>("No such game",HttpStatus.FORBIDDEN);

        }

        Game game= gameRepository.getOne(gameID);

        if(game.getGamePlayers().size()>=2){

            return new ResponseEntity<>("Game is full",HttpStatus.FORBIDDEN);

        }

        if(game.getGamePlayers().stream().map(gamePlayer -> gamePlayer.getPlayer().getUserName()).collect(Collectors.toList()).contains(authentication.getName())){

            return new ResponseEntity<>("You can't join your game",HttpStatus.FORBIDDEN);

        }

        GamePlayer gamePlayer= new GamePlayer(game, playerRepository.findByUserName(authentication.getName()));

        gamePlayerRepository.save(gamePlayer);

        Map<String,Object> dto= new LinkedHashMap<>();

        dto.put("gpid", gamePlayer.getId());

        return new ResponseEntity<>(dto, HttpStatus.ACCEPTED);

    }

    @RequestMapping("/game/{gameID}/players")
    public ResponseEntity<Object> enterGame(@PathVariable Long gameID, Authentication authentication) {

        if(isGuest(authentication)){

            return new ResponseEntity<>("You must login to enter game",HttpStatus.UNAUTHORIZED);

        }

        if(gameRepository.findById(gameID).orElse(null)==null){

            return new ResponseEntity<>("No such game",HttpStatus.FORBIDDEN);

        }

        Game game= gameRepository.getOne(gameID);

        Set<GamePlayer> gameplayers = game.getGamePlayers().stream().filter(gamePlayer -> gamePlayer.getPlayer().getUserName() == authentication.getName()).collect(Collectors.toSet());

        Map<String,Object> dto = new LinkedHashMap<>();

        dto.put("gamePlayers", getAllGamePlayers(gameplayers));

        return new ResponseEntity<>(dto, HttpStatus.ACCEPTED);

    }

    @RequestMapping(path = "/games/players/{gamePlayerId}/ships", method = RequestMethod.POST)
    public ResponseEntity<String> addShips(@PathVariable Long gamePlayerId, @RequestBody List<ShipDTO> shipsDTO, Authentication authentication) {

        if(isGuest(authentication)){

            return new ResponseEntity<>("You must login to add ships",HttpStatus.UNAUTHORIZED);

        }

        if(gamePlayerRepository.findById(gamePlayerId).orElse(null)==null){

            return new ResponseEntity<>("No such Game Player",HttpStatus.FORBIDDEN);

        }

        GamePlayer gamePlayer= gamePlayerRepository.getOne(gamePlayerId);
        
        if(gamePlayer.getPlayer().getId()!=(playerRepository.findByUserName(authentication.getName()).getId())){

            return new ResponseEntity<>("This game does not belong to you", HttpStatus.UNAUTHORIZED);

        }

        if(gamePlayer.getShips().size()>=5){

            return new ResponseEntity<>("No more ships can be added", HttpStatus.FORBIDDEN);

        }

        for (ShipDTO shipDTO:shipsDTO) {

            Ship ship= new Ship(gamePlayer, shipDTO.getLocations(), shipDTO.getType());

            shipRepository.save(ship);
            
        }

        return new ResponseEntity<>("Ships added",HttpStatus.ACCEPTED);
    }

    @RequestMapping(path = "/games/players/{gamePlayerId}/salvos", method = RequestMethod.POST)
    public ResponseEntity<String> addSalvos(@PathVariable Long gamePlayerId, @RequestBody SalvoDTO salvoDTO, Authentication authentication) {

        if(isGuest(authentication)){

            return new ResponseEntity<>("You must login to add ships",HttpStatus.UNAUTHORIZED);

        }

        if(gamePlayerRepository.findById(gamePlayerId).orElse(null)==null){

            return new ResponseEntity<>("No such Game Player",HttpStatus.FORBIDDEN);

        }

        GamePlayer gamePlayer= gamePlayerRepository.getOne(gamePlayerId);

        if(gamePlayer.getPlayer().getId()!=(playerRepository.findByUserName(authentication.getName()).getId())){

            return new ResponseEntity<>("This game does not belong to you", HttpStatus.UNAUTHORIZED);

        }

        int turn;

        if(gamePlayer.getSalvos().size() == 0){

            turn = 1;

        }else{

           turn = gamePlayer.getSalvos().size() + 1;

        }

        Salvo salvo = new Salvo(gamePlayer, salvoDTO.getLocations(), turn);

        salvoRepository.save(salvo);

        return new ResponseEntity<>("Salvo added",HttpStatus.ACCEPTED);

    }

}