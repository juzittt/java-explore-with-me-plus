package ewm.participationRequest.dto;

import ewm.participationRequest.model.RequestStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ParticipationRequestDto {
    private Long id;
    private Long requester;
    private Long event;
    private LocalDateTime created;
    private RequestStatus requestStatus;
}
