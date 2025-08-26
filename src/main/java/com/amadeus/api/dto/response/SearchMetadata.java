package com.amadeus.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchMetadata {

    private String searchId;
    private LocalDateTime searchTime;
    private Integer totalResults;
    private String currency;
}
