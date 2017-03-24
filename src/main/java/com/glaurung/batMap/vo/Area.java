package com.glaurung.batMap.vo;

import java.io.Serializable;

public class Area implements Serializable {


    private static final long serialVersionUID = 5397970358054415742L;
    private String name;

    public Area( String name ) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }


}
