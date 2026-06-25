package ewm.participationRequests.dto;

import ewm.participationRequests.model.Status;
import lombok.Data;

import java.util.List;

@Data
public class EventRequestStatusUpdateRequest {
    List<Long> requestIds;
    Status status;
}
