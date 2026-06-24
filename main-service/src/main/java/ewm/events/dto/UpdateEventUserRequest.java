package ewm.events.dto;

import ewm.events.model.Location;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventUserRequest {
    private String annotation;
    private Long category;
    private String description;
    private String eventDate;
    private Location location;
    private Boolean paid;
    private Integer participantLimit;
    private Boolean requestModeration;

    /*
     * Из свагера:
     * * Изменение состояния события
     * * Enum:
     * * [ SEND_TO_REVIEW, CANCEL_REVIEW ]
     */
    private String stateAction;
    private String title;
}