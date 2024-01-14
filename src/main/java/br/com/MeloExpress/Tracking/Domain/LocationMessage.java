package br.com.MeloExpress.Tracking.Domain;

import lombok.*;

@Getter
@Setter
@Data
public class LocationMessage {


    private double longitude;
    private double latitude;

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
