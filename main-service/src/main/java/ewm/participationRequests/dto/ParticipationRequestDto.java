package ewm.participationRequests.dto;

import ewm.participationRequests.model.Status;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ParticipationRequestDto {
    private Long id;
    private Long requester;
    private Long event;
    private LocalDateTime created;
    private Status status;
}
