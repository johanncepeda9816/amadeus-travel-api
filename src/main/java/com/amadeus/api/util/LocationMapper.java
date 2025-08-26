package com.amadeus.api.util;

import com.amadeus.api.dto.response.LocationDto;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class LocationMapper {

    private static final Map<String, String> LOCATION_NAMES = new HashMap<>();

    static {
        LOCATION_NAMES.put("BOGOTA", "Bogotá");
        LOCATION_NAMES.put("MEDELLIN", "Medellín");
        LOCATION_NAMES.put("CALI", "Cali");
        LOCATION_NAMES.put("CARTAGENA", "Cartagena");
        LOCATION_NAMES.put("BARRANQUILLA", "Barranquilla");
        LOCATION_NAMES.put("BUCARAMANGA", "Bucaramanga");
        LOCATION_NAMES.put("PEREIRA", "Pereira");
        LOCATION_NAMES.put("SANTA_MARTA", "Santa Marta");
        LOCATION_NAMES.put("MANIZALES", "Manizales");
        LOCATION_NAMES.put("VILLAVICENCIO", "Villavicencio");
        LOCATION_NAMES.put("NEIVA", "Neiva");
        LOCATION_NAMES.put("MONTERIA", "Montería");
        LOCATION_NAMES.put("VALLEDUPAR", "Valledupar");
        LOCATION_NAMES.put("CUCUTA", "Cúcuta");
        LOCATION_NAMES.put("ARMENIA", "Armenia");
        LOCATION_NAMES.put("IBAGUE", "Ibagué");
        LOCATION_NAMES.put("PASTO", "Pasto");
        LOCATION_NAMES.put("POPAYAN", "Popayán");
        LOCATION_NAMES.put("SINCELEJO", "Sincelejo");
        LOCATION_NAMES.put("QUIBDO", "Quibdó");

        LOCATION_NAMES.put("MADRID", "Madrid");
        LOCATION_NAMES.put("BARCELONA", "Barcelona");
        LOCATION_NAMES.put("PARIS", "París");
        LOCATION_NAMES.put("LONDON", "Londres");
        LOCATION_NAMES.put("ROME", "Roma");
        LOCATION_NAMES.put("AMSTERDAM", "Ámsterdam");
        LOCATION_NAMES.put("FRANKFURT", "Frankfurt");
        LOCATION_NAMES.put("ZURICH", "Zurich");
        LOCATION_NAMES.put("VIENNA", "Viena");
        LOCATION_NAMES.put("LISBON", "Lisboa");

        LOCATION_NAMES.put("NEW_YORK", "Nueva York");
        LOCATION_NAMES.put("MIAMI", "Miami");
        LOCATION_NAMES.put("LOS_ANGELES", "Los Ángeles");
        LOCATION_NAMES.put("MEXICO_CITY", "Ciudad de México");
        LOCATION_NAMES.put("LIMA", "Lima");
        LOCATION_NAMES.put("QUITO", "Quito");
        LOCATION_NAMES.put("CARACAS", "Caracas");
        LOCATION_NAMES.put("PANAMA_CITY", "Ciudad de Panamá");
        LOCATION_NAMES.put("SAN_JOSE", "San José");
        LOCATION_NAMES.put("GUATEMALA_CITY", "Ciudad de Guatemala");
    }

    public LocationDto mapToLocationDto(String code) {
        String friendlyName = LOCATION_NAMES.getOrDefault(code, formatCode(code));
        return LocationDto.builder()
                .code(code)
                .name(friendlyName)
                .build();
    }

    private String formatCode(String code) {
        if (code == null || code.isEmpty()) {
            return code;
        }

        return code.replace("_", " ")
                .toLowerCase()
                .substring(0, 1).toUpperCase() +
                code.replace("_", " ").toLowerCase().substring(1);
    }
}
