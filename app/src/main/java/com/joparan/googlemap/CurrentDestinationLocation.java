package com.joparan.googlemap;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.IgnoreExtraProperties;

public class CurrentDestinationLocation {
    public LatLng currentLocation;
    public LatLng destinationLocation;

    public CurrentDestinationLocation() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public CurrentDestinationLocation(LatLng currentLocation, LatLng destinationLocation) {
        this.currentLocation = currentLocation;
        this.destinationLocation = destinationLocation;
    }
    public LatLng getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(LatLng currentLocation) {
        this.currentLocation = currentLocation;
    }

    public LatLng getDestinationLocation() {
        return destinationLocation;
    }

    public void setDestinationLocation(LatLng destinationLocation) {
        this.destinationLocation = destinationLocation;
    }

    @Override
    public String toString() {
        return "User{" +
                "Current Location='" + currentLocation + '\'' +
                ", Destination Location='" + destinationLocation + '\'' +
                '}';
    }
}
