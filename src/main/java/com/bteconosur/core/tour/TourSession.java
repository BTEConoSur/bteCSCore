package com.bteconosur.core.tour;

import java.util.List;

import org.bukkit.Location;

public class TourSession {

    private final Location returnLocation;
    private int currentIndex = 1;

    private List<String> projectIds;
    
    private String tourId;
    
    public TourSession(String tourId, Location returnLocation) {
        this.tourId = tourId;
        this.returnLocation = returnLocation;
    }

    public TourSession(List<String> projectIds, Location returnLocation) {
        this.projectIds = projectIds;
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

    public boolean isProjectTour() {
        return projectIds != null;
    }

    public String getCurrentProjectId() {
        if (!isProjectTour() || currentIndex < 1 || currentIndex > projectIds.size()) return null;
        return projectIds.get(currentIndex - 1);
    }

    public List<String> getProjectIds() {
        return projectIds;
    }

}
