package com.codeoftheweb.salvo.entities;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Entity
public class Ship {

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

  private String type;

  public Ship() {

  }

  public Ship(GamePlayer gamePlayer, List<String> locations, String type) {
    this.gamePlayer = gamePlayer;
    this.locations = locations;
    this.type = type;
  }

  public GamePlayer getGamePlayer() {
    return gamePlayer;
  }

  public List<String> getLocations() {
    return locations;
  }

  public String getType() {
    return type;
  }

  public Object getDTO(){

    Map<String,Object> dto = new LinkedHashMap<>();

    dto.put("type",this.getType());
    dto.put("locations", this.getLocations());

    return dto;

  }
}
