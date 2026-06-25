package ewm.events.service;

import client.StatsClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ewm.categories.model.Category;
import ewm.categories.repository.CategoryRepository;
import ewm.dto.EndpointHitDto;
import ewm.dto.ViewStatsDto;
import ewm.events.dto.*;
import ewm.events.dto.params.AdminEventParams;
import ewm.events.dto.params.PaginationParams;
import ewm.events.dto.params.PublicEventParams;
import ewm.events.dto.params.UserEventPathParams;
import ewm.events.mapper.EventMapper;
import ewm.events.model.Event;
import ewm.events.model.State;
import ewm.events.repository.EventsRepository;
import ewm.events.repository.specification.EventSpecification;
import ewm.exception.ConflictException;
import ewm.exception.NotFoundException;
import ewm.exception.ValidationException;
import ewm.users.model.User;
import ewm.users.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EventsServiceImpl implements EventsService {

    private final EventsRepository eventsRepository;
    private final EventMapper eventMapper;
    private final StatsClient statsClient;
    private final ObjectMapper objectMapper;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);
    private static final String APP_NAME = "ewm-main-service";

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

        sendHitToStats("/events", request.getRemoteAddr());

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
        List<Event> events = page.getContent();

        if (events.isEmpty()) {
            return Collections.emptyList();
        }

        List<EventShortDto> dtos = eventMapper.toEventShortDtoList(events);
        enrichEventsWithStats(dtos);

        return dtos;
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

        List<Event> events = page.getContent();

        if (events.isEmpty()) {
            return Collections.emptyList();
        }

        List<EventFullDto> eventFullDtoList = eventMapper.toEventFullDtoList(page.getContent());
        enrichEventFullDtosWithStats(eventFullDtoList);

        return eventFullDtoList;
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEvent(Long id, HttpServletRequest request) {
        Event event = eventsRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event с id=" + id + " не найден"));

        if (event.getState() != State.PUBLISHED) {
            throw new NotFoundException("Event с id=" + id + " не найден");
        }

        /*
         * Не уверен, нужно тестить
         */
        sendHitToStats("/events/" + id, request.getRemoteAddr());

        EventFullDto dto = eventMapper.toEventFullDto(event);
        enrichEventWithStats(dto);

        return dto;
    }

    @Override
    public EventFullDto createEvent(Long userId, NewEventDto dto) {
        User user = getUserOrThrow(userId);
        Category category = getCategoryOrThrow(dto.getCategory());

        LocalDateTime eventDate = parseAndValidateEventDate(
                dto.getEventDate(),
                "Дата события должна быть не ранее чем через 2 часа от текущего момента"
        );

        Event event = eventMapper.toEvent(dto, category, user);
        event.setCreatedOn(LocalDateTime.now());
        event.setState(State.PENDING);
        event.setEventDate(eventDate);

        event = eventsRepository.save(event);

        EventFullDto result = eventMapper.toEventFullDto(event);
        result.setViews(0L);
        result.setConfirmedRequests(0L);

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getUserEvent(UserEventPathParams params) {
        Event event = eventsRepository.findByIdAndInitiatorId(params.getEventId(), params.getUserId())
                .orElseThrow(() -> new NotFoundException("Event с id=" + params.getEventId() + " не найден"));

        EventFullDto dto = eventMapper.toEventFullDto(event);
        enrichEventWithStats(dto);

        return dto;
    }


    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getUserEvents(Long userId, PaginationParams params) {
        getUserOrThrow(userId);

        PageRequest pageRequest = PageRequest.of(params.getFrom() / params.getSize(), params.getSize());
        Page<Event> page = eventsRepository.findByInitiatorId(userId, pageRequest);
        List<Event> events = page.getContent();

        if (events.isEmpty()) {
            return Collections.emptyList();
        }

        List<EventShortDto> dtos = eventMapper.toEventShortDtoList(events);
        enrichEventsWithStats(dtos);

        return dtos;
    }

    @Override
    public EventFullDto updateAdminEvent(Long eventId, UpdateEventAdminRequest request) {
        return null;
    }

    @Override
    public EventFullDto updateUserEvent(UserEventPathParams params, UpdateEventUserRequest dto) {
        Event event = eventsRepository.findByIdAndInitiatorId(params.getEventId(), params.getUserId())
                .orElseThrow(() -> new NotFoundException("Event с id=" + params.getEventId() + " не найден"));

        if (event.getState() == State.PUBLISHED) {
            throw new ConflictException("Можно изменять только отмененные события или события в состоянии ожидания модерации");
        }

        if (dto.getEventDate() != null) {
            LocalDateTime eventDate = parseAndValidateEventDate(
                    dto.getEventDate(),
                    "Дата события должна быть не ранее чем через 2 часа от текущего момента"
            );
            event.setEventDate(eventDate);
        }

        if (dto.getAnnotation() != null) event.setAnnotation(dto.getAnnotation());
        if (dto.getDescription() != null) event.setDescription(dto.getDescription());
        if (dto.getTitle() != null) event.setTitle(dto.getTitle());
        if (dto.getPaid() != null) event.setPaid(dto.getPaid());
        if (dto.getParticipantLimit() != null) event.setParticipantLimit(dto.getParticipantLimit());
        if (dto.getRequestModeration() != null) event.setRequestModeration(dto.getRequestModeration());

        if (dto.getCategory() != null) {
            Category category = getCategoryOrThrow(dto.getCategory());
            event.setCategory(category);
        }

        if (dto.getLocation() != null) {
            event.getLocation().setLat(dto.getLocation().getLat());
            event.getLocation().setLon(dto.getLocation().getLon());
        }

        if (dto.getStateAction() != null) {
            switch (dto.getStateAction()) {
                case StateAction.SEND_TO_REVIEW -> event.setState(State.PENDING);
                case StateAction.CANCEL_REVIEW -> event.setState(State.CANCELED);
                default -> throw new ValidationException("Неизвестное состояние: " + dto.getStateAction());
            }
        }

        event = eventsRepository.save(event);

        EventFullDto result = eventMapper.toEventFullDto(event);
        enrichEventWithStats(result);

        return result;
    }

    private void sendHitToStats(String uri, String ip) {
        statsClient.saveHit(EndpointHitDto.builder()
                .app(APP_NAME)
                .uri(uri)
                .ip(ip)
                .timestamp(LocalDateTime.now())
                .build());
    }

    private Map<Long, Long> getViewsMap(List<Long> eventIds) {

        if (eventIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<String> uris = eventIds.stream()
                .map(id -> "/events/" + id)
                .toList();

        ResponseEntity<Object> response = statsClient.getStats(
                LocalDateTime.of(2000, 1, 1, 0, 0),
                LocalDateTime.now(),
                uris,
                true
        );

        List<ViewStatsDto> stats = objectMapper.convertValue(
                response.getBody(),
                new TypeReference<List<ViewStatsDto>>() {
                }
        );

        return stats.stream()
                .collect(Collectors.toMap(
                        stat -> Long.parseLong(
                                stat.getUri().substring(
                                        stat.getUri().lastIndexOf('/') + 1
                                )
                        ),
                        ViewStatsDto::getHits
                ));

    }

    private Map<Long, Long> getConfirmedRequestsMap(List<Long> eventIds) {
        //нужно дописать после реализации запросов на участие
        return Collections.emptyMap();
    }

    private void enrichEventWithStats(EventFullDto dto) {
        Map<Long, Long> viewsMap = getViewsMap(List.of(dto.getId()));
        Map<Long, Long> confirmedMap = getConfirmedRequestsMap(List.of(dto.getId()));

        dto.setViews(viewsMap.getOrDefault(dto.getId(), 0L));
        dto.setConfirmedRequests(confirmedMap.getOrDefault(dto.getId(), 0L));
    }

    private void enrichEventsWithStats(List<EventShortDto> dtos) {
        if (dtos.isEmpty()) {
            return;
        }

        List<Long> eventIds = dtos.stream()
                .map(EventShortDto::getId)
                .toList();

        Map<Long, Long> viewsMap = getViewsMap(eventIds);
        Map<Long, Long> confirmedMap = getConfirmedRequestsMap(eventIds);

        dtos.forEach(dto -> {
            dto.setViews(viewsMap.getOrDefault(dto.getId(), 0L));
            dto.setConfirmedRequests(confirmedMap.getOrDefault(dto.getId(), 0L));
        });
    }

    private void enrichEventFullDtosWithStats(List<EventFullDto> dtos) {
        if (dtos.isEmpty()) {
            return;
        }

        List<Long> eventIds = dtos.stream()
                .map(EventFullDto::getId)
                .toList();

        Map<Long, Long> viewsMap = getViewsMap(eventIds);
        Map<Long, Long> confirmedMap = getConfirmedRequestsMap(eventIds);

        dtos.forEach(dto -> {
            dto.setViews(viewsMap.getOrDefault(dto.getId(), 0L));
            dto.setConfirmedRequests(confirmedMap.getOrDefault(dto.getId(), 0L));
        });
    }

    private LocalDateTime parseAndValidateEventDate(String dateStr, String errorMessage) {
        LocalDateTime eventDate = LocalDateTime.parse(dateStr, FORMATTER);

        if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ConflictException(errorMessage);
        }

        return eventDate;
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User с id=" + userId + " не найден"));
    }

    private Category getCategoryOrThrow(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category с id=" + categoryId + " не найдена"));
    }
}