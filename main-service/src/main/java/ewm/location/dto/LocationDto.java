package ewm.location.dto;

import ewm.location.model.LocationType;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationDto {
    private Long id;
    private Float lat;
    private Float lon;
    private String name;
    private String description;
    private LocationType locationType;
    private Double radiusMeters;
}
