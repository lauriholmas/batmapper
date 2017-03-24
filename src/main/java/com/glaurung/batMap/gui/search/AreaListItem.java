package com.glaurung.batMap.gui.search;

import com.glaurung.batMap.vo.Room;

public class AreaListItem {
    private Room room;

    public AreaListItem( Room room ) {
        this.room = room;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom( Room room ) {
        this.room = room;
    }

    @Override
    public String toString() {
        return this.room.getArea().getName();
    }
}
