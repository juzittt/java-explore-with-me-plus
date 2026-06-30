package ewm.participationRequest.repository;

import ewm.participationRequest.dto.EventRequestsCountDto;
import ewm.participationRequest.model.ParticipationRequest;
import ewm.participationRequest.model.RequestStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ParticipationRequestsRepository
        extends JpaRepository<ParticipationRequest, Long> {

    @Query("""
            select new ewm.participationRequest.dto.EventRequestsCountDto(
                r.event.id,
                count(r.id)
            )
            from ParticipationRequest r
            where r.requestStatus = ewm.participationRequest.model.RequestStatus.CONFIRMED
              and r.event.id in :eventIds
            group by r.event.id
            """)
    List<EventRequestsCountDto> getConfirmedRequests(@Param("eventIds") List<Long> eventIds);

    @EntityGraph(attributePaths = {"requester", "event", "event.initiator"})
    List<ParticipationRequest> findAllByRequester_Id(Long requesterId);

    @EntityGraph(attributePaths = {"requester", "event", "event.initiator"})
    Optional<ParticipationRequest> findByIdAndRequester_Id(Long id, Long requesterId);

    boolean existsByRequester_IdAndEvent_Id(Long requesterId, Long eventId);

    long countByEvent_IdAndRequestStatus(Long eventId, RequestStatus requestStatus);
}
