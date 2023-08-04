package br.com.MeloExpress.Tracking.Controller;

import br.com.MeloExpress.Tracking.Domain.LocationMessage;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
public class LocationController {

    @Autowired
    private KafkaTemplate<String, LocationMessage> kafkaTemplate;

    private double lastLatitude;
    private double lastLongitude;

    private static final String TOPIC = "vehicle-location";

    private static final String GOOGLE_MAPS_API_KEY = "AIzaSyCBazjtHTeOM0e9rFxNUx5skliooRSobCM";
    private final GeoApiContext geoApiContext = new GeoApiContext.Builder().apiKey(GOOGLE_MAPS_API_KEY).build();

    private String lastFormattedAddress;

    @PostMapping("/sendLocation")
    public void sendLocationMessage(@RequestBody LocationMessage locationMessage) {
        kafkaTemplate.send(TOPIC, locationMessage);
    }

    @GetMapping("/currentLocation")
    public String getCurrentLocation() {
        if (lastFormattedAddress != null) {
            return lastFormattedAddress;
        } else {
            return "Localização atual não encontrada.";
        }
    }

    @GetMapping("/currentLocationMap")
    public String getCurrentLocationMap() {
        if (lastLatitude != 0 && lastLongitude != 0) {
            return "https://www.google.com/maps?q=" + lastLatitude + "," + lastLongitude;
        } else {
            return "Localização atual não encontrada.";
        }
    }

    @KafkaListener(topics = TOPIC, groupId = "${spring.kafka.consumer.group-id}")
    public void receiveLocationMessage(LocationMessage locationMessage) {
        // Geocodificação para obter o endereço da localização
        try {
            GeocodingResult[] results = GeocodingApi.newRequest(geoApiContext)
                    .latlng(new com.google.maps.model.LatLng(locationMessage.getLatitude(), locationMessage.getLongitude()))
                    .await();

            if (results != null && results.length > 0) {
                String formattedAddress = results[0].formattedAddress;
                lastFormattedAddress = formattedAddress;
                lastLatitude = locationMessage.getLatitude();
                lastLongitude = locationMessage.getLongitude();
            } else {
                lastFormattedAddress = "Endereço não encontrado.";
                lastLatitude = 0;
                lastLongitude = 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            lastFormattedAddress = "Erro ao obter o endereço.";
            lastLatitude = 0;
            lastLongitude = 0;
        }
    }
}




