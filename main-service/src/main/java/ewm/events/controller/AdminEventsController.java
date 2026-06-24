package ewm.events.controller;

import ewm.events.dto.EventFullDto;
import ewm.events.dto.params.AdminEventParams;
import ewm.events.service.EventsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
public class AdminEventsController {

    public final EventsService eventsService;

    @GetMapping
    public ResponseEntity<List<EventFullDto>> getEvents(AdminEventParams params) {
        return ResponseEntity.ok(eventsService.getAdminEvents(params));
    }

}
