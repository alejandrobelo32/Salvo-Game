package com.codeoftheweb.salvo.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;
    private String userName;
    private String password;

    @OneToMany(mappedBy = "player", fetch = FetchType.EAGER)
    Set<GamePlayer> gamePlayers;

    @OneToMany(mappedBy = "player", fetch = FetchType.EAGER)
    private Set<Score> scores;

    public Player() { }

    public long getId() {
        return id;
    }

    public Player(String userName, String password) {
        this.userName = userName.toLowerCase();
        this.password=password;
    }


    public String getUserName() {
        return this.userName;
    }

    @JsonIgnore
    public Set<Score> getScores() {
        return scores;
    }

    public String getPassword() {
        return password;
    }

    public Map<String,Object> getDTO() {

        Map<String,Object> dto = new LinkedHashMap<>();

        dto.put("id",this.getId());
        dto.put("mail", this.getUserName());

        return dto;

    }

    public double getTotalScore() {

        return getScoresList()
                .stream()
                .reduce((double) 0, (score1, score2) -> {
                    return score1 + score2;
                });

    }

    public long getWins() {

        return getScoresList()
                .stream()
                .filter(score -> score==1.0).count();

    }

    public long getLooses() {

        return getScoresList().stream()
                .filter(score -> score == 0.0).count();

    }

    public long getTied() {

        return getScoresList().stream()
                .filter(score -> score == 0.5).count();

    }

    private List<Double> getScoresList(){

        return this.scores.stream()
                .map(score -> score.getScore())
                .collect(Collectors.toList());

    }

}
