package ewm.events.service;

import ewm.events.dto.EventFullDto;
import ewm.events.dto.EventShortDto;
import ewm.events.dto.params.AdminEventParams;
import ewm.events.dto.params.PublicEventParams;
import ewm.events.mapper.EventMapper;
import ewm.events.model.Event;
import ewm.events.model.State;
import ewm.events.repository.EventsRepository;
import ewm.events.repository.specification.EventSpecification;
import ewm.exception.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EventsServiceImpl implements EventsService {

    private final EventsRepository eventsRepository;
    private final EventMapper eventMapper;

    private Sort buildSort(PublicEventParams params) {

        if (params.getSort() == null) {
            return Sort.unsorted();
        }

        return switch (params.getSort()) {

            case EVENT_DATE -> Sort.by(Sort.Direction.ASC, "eventDate");

            case VIEWS -> Sort.unsorted(); //Временная заглушка
        };
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getEvents(PublicEventParams params, HttpServletRequest request) {

        LocalDateTime startDate = params.getRangeStart();
        LocalDateTime endDate = params.getRangeEnd();

        if (startDate == null && params.getRangeEnd() == null) {
            startDate = LocalDateTime.now();
        }

        Sort sort = buildSort(params);

        PageRequest pageRequest = PageRequest.of(
                params.getFrom() / params.getSize(),
                params.getSize(),
                sort
        );

        Specification<Event> spec = Specification
                .where(EventSpecification.published())
                .and(EventSpecification.hasText(params.getText()))
                .and(EventSpecification.hasCategories(params.getCategories()))
                .and(EventSpecification.hasPaid(params.getPaid()))
                .and(EventSpecification.onlyAvailable(params.getOnlyAvailable()))
                .and(EventSpecification.dateAfter(startDate))
                .and(EventSpecification.dateBefore(endDate));

        Page<Event> page = eventsRepository.findAll(spec, pageRequest);

        return eventMapper.toEventShortDtoList(page.getContent());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> getAdminEvents(AdminEventParams params) {

        LocalDateTime startDate = params.getRangeStart();
        LocalDateTime endDate = params.getRangeEnd();

        PageRequest pageRequest = PageRequest.of(
                params.getFrom() / params.getSize(),
                params.getSize()
        );

        List<State> states = params.getStates() == null
                ? null
                : params.getStates().stream()
                  .map(State::valueOf)
                  .toList();

        Specification<Event> spec = Specification
                .where(EventSpecification.hasUser(params.getUsers()))
                .and(EventSpecification.hasStates(states)
                .and(EventSpecification.hasCategories(params.getCategories()))
                .and(EventSpecification.dateAfter(startDate))
                .and(EventSpecification.dateBefore(endDate)));

        Page<Event> page = eventsRepository.findAll(spec, pageRequest);

        return eventMapper.toEventFullDtoList(page.getContent());
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEvent(Long id, HttpServletRequest request) {
        Event event = eventsRepository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException("Event с id=" + id + " не найден"));

        if (event.getState() != State.PUBLISHED) {
            throw new NotFoundException("Event с id=" + id + " не найден");
        }

        EventFullDto dto = eventMapper.toEventFullDto(event);

        dto.setViews(0L);
        dto.setConfirmedRequests(0L);

        return dto;
    }
}
