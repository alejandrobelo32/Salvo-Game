package com.codeoftheweb.salvo.dto;

import java.util.List;

public class ShipDTO {

    private String type;
    private List<String> locations;

    public ShipDTO(String type, List<String> locations) {
        this.type = type;
        this.locations = locations;
    }

    public String getType() {
        return type;
    }

    public List<String> getLocations(){
        return locations;
    }
}
