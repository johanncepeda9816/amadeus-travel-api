package com.amadeus.api.util;

import com.amadeus.api.dto.response.LocationDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LocationMapperTest {

    private LocationMapper locationMapper;

    @BeforeEach
    void setUp() {
        locationMapper = new LocationMapper();
    }

    @Test
    void mapToLocationDto_ShouldReturnCorrectMapping_ForColombianCities() {
        LocationDto bogota = locationMapper.mapToLocationDto("BOGOTA");
        assertThat(bogota.getCode()).isEqualTo("BOGOTA");
        assertThat(bogota.getName()).isEqualTo("Bogotá");

        LocationDto medellin = locationMapper.mapToLocationDto("MEDELLIN");
        assertThat(medellin.getCode()).isEqualTo("MEDELLIN");
        assertThat(medellin.getName()).isEqualTo("Medellín");

        LocationDto cartagena = locationMapper.mapToLocationDto("CARTAGENA");
        assertThat(cartagena.getCode()).isEqualTo("CARTAGENA");
        assertThat(cartagena.getName()).isEqualTo("Cartagena");
    }

    @Test
    void mapToLocationDto_ShouldReturnCorrectMapping_ForEuropeanCities() {
        LocationDto madrid = locationMapper.mapToLocationDto("MADRID");
        assertThat(madrid.getCode()).isEqualTo("MADRID");
        assertThat(madrid.getName()).isEqualTo("Madrid");

        LocationDto paris = locationMapper.mapToLocationDto("PARIS");
        assertThat(paris.getCode()).isEqualTo("PARIS");
        assertThat(paris.getName()).isEqualTo("París");

        LocationDto london = locationMapper.mapToLocationDto("LONDON");
        assertThat(london.getCode()).isEqualTo("LONDON");
        assertThat(london.getName()).isEqualTo("Londres");

        LocationDto amsterdam = locationMapper.mapToLocationDto("AMSTERDAM");
        assertThat(amsterdam.getCode()).isEqualTo("AMSTERDAM");
        assertThat(amsterdam.getName()).isEqualTo("Ámsterdam");
    }

    @Test
    void mapToLocationDto_ShouldReturnCorrectMapping_ForAmericanCities() {
        LocationDto newYork = locationMapper.mapToLocationDto("NEW_YORK");
        assertThat(newYork.getCode()).isEqualTo("NEW_YORK");
        assertThat(newYork.getName()).isEqualTo("Nueva York");

        LocationDto miami = locationMapper.mapToLocationDto("MIAMI");
        assertThat(miami.getCode()).isEqualTo("MIAMI");
        assertThat(miami.getName()).isEqualTo("Miami");

        LocationDto losAngeles = locationMapper.mapToLocationDto("LOS_ANGELES");
        assertThat(losAngeles.getCode()).isEqualTo("LOS_ANGELES");
        assertThat(losAngeles.getName()).isEqualTo("Los Ángeles");

        LocationDto mexicoCity = locationMapper.mapToLocationDto("MEXICO_CITY");
        assertThat(mexicoCity.getCode()).isEqualTo("MEXICO_CITY");
        assertThat(mexicoCity.getName()).isEqualTo("Ciudad de México");
    }

    @Test
    void mapToLocationDto_ShouldReturnCorrectMapping_ForSouthAmericanCities() {
        LocationDto lima = locationMapper.mapToLocationDto("LIMA");
        assertThat(lima.getCode()).isEqualTo("LIMA");
        assertThat(lima.getName()).isEqualTo("Lima");

        LocationDto quito = locationMapper.mapToLocationDto("QUITO");
        assertThat(quito.getCode()).isEqualTo("QUITO");
        assertThat(quito.getName()).isEqualTo("Quito");

        LocationDto caracas = locationMapper.mapToLocationDto("CARACAS");
        assertThat(caracas.getCode()).isEqualTo("CARACAS");
        assertThat(caracas.getName()).isEqualTo("Caracas");
    }

    @Test
    void mapToLocationDto_ShouldReturnFormattedName_ForUnknownCodes() {
        LocationDto unknown = locationMapper.mapToLocationDto("UNKNOWN_CITY");
        assertThat(unknown.getCode()).isEqualTo("UNKNOWN_CITY");
        assertThat(unknown.getName()).isEqualTo("Unknown city");

        LocationDto singleWord = locationMapper.mapToLocationDto("TOKYO");
        assertThat(singleWord.getCode()).isEqualTo("TOKYO");
        assertThat(singleWord.getName()).isEqualTo("Tokyo");

        LocationDto multipleWords = locationMapper.mapToLocationDto("SAN_FRANCISCO_BAY");
        assertThat(multipleWords.getCode()).isEqualTo("SAN_FRANCISCO_BAY");
        assertThat(multipleWords.getName()).isEqualTo("San francisco bay");
    }

    @Test
    void mapToLocationDto_ShouldHandleNullAndEmptyInputs() {
        LocationDto nullInput = locationMapper.mapToLocationDto(null);
        assertThat(nullInput.getCode()).isNull();
        assertThat(nullInput.getName()).isNull();

        LocationDto emptyInput = locationMapper.mapToLocationDto("");
        assertThat(emptyInput.getCode()).isEmpty();
        assertThat(emptyInput.getName()).isEmpty();
    }

    @Test
    void mapToLocationDto_ShouldHandleSpecialColombianCities() {
        LocationDto santaMarta = locationMapper.mapToLocationDto("SANTA_MARTA");
        assertThat(santaMarta.getCode()).isEqualTo("SANTA_MARTA");
        assertThat(santaMarta.getName()).isEqualTo("Santa Marta");

        LocationDto cucuta = locationMapper.mapToLocationDto("CUCUTA");
        assertThat(cucuta.getCode()).isEqualTo("CUCUTA");
        assertThat(cucuta.getName()).isEqualTo("Cúcuta");

        LocationDto ibague = locationMapper.mapToLocationDto("IBAGUE");
        assertThat(ibague.getCode()).isEqualTo("IBAGUE");
        assertThat(ibague.getName()).isEqualTo("Ibagué");

        LocationDto popayan = locationMapper.mapToLocationDto("POPAYAN");
        assertThat(popayan.getCode()).isEqualTo("POPAYAN");
        assertThat(popayan.getName()).isEqualTo("Popayán");

        LocationDto quibdo = locationMapper.mapToLocationDto("QUIBDO");
        assertThat(quibdo.getCode()).isEqualTo("QUIBDO");
        assertThat(quibdo.getName()).isEqualTo("Quibdó");
    }

    @Test
    void mapToLocationDto_ShouldHandleSpecialEuropeanCities() {
        LocationDto zurich = locationMapper.mapToLocationDto("ZURICH");
        assertThat(zurich.getCode()).isEqualTo("ZURICH");
        assertThat(zurich.getName()).isEqualTo("Zurich");

        LocationDto vienna = locationMapper.mapToLocationDto("VIENNA");
        assertThat(vienna.getCode()).isEqualTo("VIENNA");
        assertThat(vienna.getName()).isEqualTo("Viena");

        LocationDto lisbon = locationMapper.mapToLocationDto("LISBON");
        assertThat(lisbon.getCode()).isEqualTo("LISBON");
        assertThat(lisbon.getName()).isEqualTo("Lisboa");

        LocationDto frankfurt = locationMapper.mapToLocationDto("FRANKFURT");
        assertThat(frankfurt.getCode()).isEqualTo("FRANKFURT");
        assertThat(frankfurt.getName()).isEqualTo("Frankfurt");
    }

    @Test
    void mapToLocationDto_ShouldHandleCentralAmericanCities() {
        LocationDto panamaCity = locationMapper.mapToLocationDto("PANAMA_CITY");
        assertThat(panamaCity.getCode()).isEqualTo("PANAMA_CITY");
        assertThat(panamaCity.getName()).isEqualTo("Ciudad de Panamá");

        LocationDto sanJose = locationMapper.mapToLocationDto("SAN_JOSE");
        assertThat(sanJose.getCode()).isEqualTo("SAN_JOSE");
        assertThat(sanJose.getName()).isEqualTo("San José");

        LocationDto guatemalaCity = locationMapper.mapToLocationDto("GUATEMALA_CITY");
        assertThat(guatemalaCity.getCode()).isEqualTo("GUATEMALA_CITY");
        assertThat(guatemalaCity.getName()).isEqualTo("Ciudad de Guatemala");
    }

    @Test
    void mapToLocationDto_ShouldFormatUnknownCodes() {
        LocationDto lowercase = locationMapper.mapToLocationDto("bogota");
        assertThat(lowercase.getCode()).isEqualTo("bogota");
        assertThat(lowercase.getName()).isEqualTo("Bogota");

        LocationDto mixedCase = locationMapper.mapToLocationDto("MaDrId");
        assertThat(mixedCase.getCode()).isEqualTo("MaDrId");
        assertThat(mixedCase.getName()).isEqualTo("Madrid");
    }

    @Test
    void mapToLocationDto_ShouldHandleEdgeCases() {
        LocationDto singleChar = locationMapper.mapToLocationDto("A");
        assertThat(singleChar.getCode()).isEqualTo("A");
        assertThat(singleChar.getName()).isEqualTo("A");

        LocationDto withNumbers = locationMapper.mapToLocationDto("CITY_123");
        assertThat(withNumbers.getCode()).isEqualTo("CITY_123");
        assertThat(withNumbers.getName()).isEqualTo("City 123");
    }

    @Test
    void mapToLocationDto_ShouldReturnBuilderBasedObject() {
        LocationDto result = locationMapper.mapToLocationDto("BOGOTA");

        assertThat(result).isNotNull();
        assertThat(result.getCode()).isNotNull();
        assertThat(result.getName()).isNotNull();
    }
}
