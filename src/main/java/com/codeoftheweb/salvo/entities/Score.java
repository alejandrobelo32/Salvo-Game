package com.codeoftheweb.salvo.entities;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@Entity
public class Score {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "game_id")
    private Game game;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "player_id")
    private Player player;

    private double score;

    private Date finishDate;

    public Score() {
    }

    public Score(Game game, Player player, double score) {
        this.game = game;
        this.player = player;
        this.score = score;
        this.finishDate = new Date();
    }

    public Player getPlayer() {
        return player;
    }

    public double getScore() {
        return score;
    }

    public Object getDTO() {

        Map<String,Object> dto = new LinkedHashMap<>();

        dto.put("player", this.getPlayer().getUserName());
        dto.put("score",this.getScore());

        return dto;

    }

}
