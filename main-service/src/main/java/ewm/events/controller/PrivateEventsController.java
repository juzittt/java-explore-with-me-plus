package ewm.events.controller;


import ewm.events.service.EventsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users/{userId/events}")
@RequiredArgsConstructor
public class PrivateEventsController {

    private final EventsService eventsService;
}
