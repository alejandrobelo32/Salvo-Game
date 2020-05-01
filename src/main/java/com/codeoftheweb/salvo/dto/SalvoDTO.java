package com.codeoftheweb.salvo.dto;

import java.util.List;

public class SalvoDTO {

    private List<String> locations;

    public SalvoDTO(){}

    public SalvoDTO(List<String> locations) {

        this.locations = locations;

    }

    public List<String> getLocations(){
        return locations;
    }
}
