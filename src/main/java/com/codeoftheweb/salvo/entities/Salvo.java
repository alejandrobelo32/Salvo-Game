package com.codeoftheweb.salvo.entities;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Entity
public class Salvo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gamePlayer_id")
    private GamePlayer gamePlayer;

    @ElementCollection
    @Column(name="locations")
    private List<String> locations;

    private int turn;

    public Salvo(){}

    public Salvo(GamePlayer gamePlayer, List<String> locations, int turn) {
        this.gamePlayer = gamePlayer;
        this.locations = locations;
        this.turn = turn;
    }

    public long getId() {
        return id;
    }

    public GamePlayer getGamePlayer() {
        return gamePlayer;
    }

    public List<String> getLocations() {
        return locations;
    }

    public int getTurn() {
        return turn;
    }

    public Map<String, Object> getDTO() {

        Map<String,Object> dto = new LinkedHashMap<>();

        dto.put("turn",this.getTurn());
        dto.put("gamePlayer", this.getGamePlayer().getId());
        dto.put("salvoLocations",this.getLocations());

        return dto;

    }
}