package ewm.participationRequests.dto;

import ewm.events.dto.EventShortDto;
import ewm.participationRequests.model.Status;
import ewm.users.dto.UserDto;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ParticipationRequestDto {
    private Long id;
    private UserDto requester;
    private EventShortDto event;
    private LocalDateTime created;
    private Status status;
}
