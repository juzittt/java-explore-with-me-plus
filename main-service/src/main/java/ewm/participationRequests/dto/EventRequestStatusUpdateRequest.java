package ewm.participationRequests.dto;

import ewm.participationRequests.model.ParticipationStatus;
import lombok.Data;

import java.util.List;

@Data
public class EventRequestStatusUpdateRequest {
    List<Long> requestIds;
    ParticipationStatus status;
}
