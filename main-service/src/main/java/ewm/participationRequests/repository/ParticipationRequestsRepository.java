package ewm.participationRequests.repository;

import ewm.participationRequests.dto.EventRequestsCountDto;
import ewm.participationRequests.model.ParticipationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ParticipationRequestsRepository
        extends JpaRepository<ParticipationRequest, Long> {

    @Query("""
        select new ewm.participationRequests.dto.EventRequestsCountDto(
            r.event.id,
            count(r.id)
        )
        from ParticipationRequests r
        where r.status = 'CONFIRMED'
          and r.event.id in :eventIds
        group by r.event.id
        """)
    List<EventRequestsCountDto> getConfirmedRequests(List<Long> eventIds);
}
