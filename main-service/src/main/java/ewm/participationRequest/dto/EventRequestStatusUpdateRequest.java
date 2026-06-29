package ewm.participationRequest.dto;

import ewm.participationRequest.model.RequestStatus;
import lombok.Data;

import java.util.List;

@Data
public class EventRequestStatusUpdateRequest {
    List<Long> requestIds;
    RequestStatus requestStatus;
}
