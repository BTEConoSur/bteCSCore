package com.bteconosur.core.tour;

import org.bukkit.Location;

public class TourSession {

    private final String tourId;
    private final Location returnLocation;
    
    private int currentIndex = 0;

    public TourSession(String tourId, Location returnLocation) {
        this.tourId = tourId;
        this.returnLocation = returnLocation;
    }

    public String getTourId() {
        return tourId;
    }
    public Location getReturnLocation() {
        return returnLocation;
    }
    
    public int getCurrentIndex() {
        return currentIndex;
    }
    public void setCurrentIndex(int index) {
        this.currentIndex = index;
    }

}
