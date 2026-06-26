package ewm.compilations.service;

import ewm.compilations.dto.NewCompilationDto;
import ewm.compilations.dto.UpdateCompilationRequest;
import ewm.compilations.mapper.CompilationMapper;
import ewm.compilations.model.Compilation;
import ewm.compilations.repository.CompilationRepository;
import ewm.events.model.Event;
import ewm.events.repository.EventsRepository;
import ewm.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ewm.compilations.dto.CompilationDto;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventsRepository eventsRepository;
    private final CompilationMapper compilationMapper;

    @Override
    public CompilationDto save(NewCompilationDto newCompilationDto) {

        Compilation compilation = compilationMapper.toCompilationfromNewDto(newCompilationDto);

        if (newCompilationDto.getEvents() != null && !newCompilationDto.getEvents().isEmpty()) {
            compilation.setEvents(
                    new HashSet<>(
                            eventsRepository.findAllById(newCompilationDto.getEvents())
                    )
            );
        }

        return compilationMapper.toCompilationDto(compilationRepository.save(compilation));
    }

    @Override
    public CompilationDto update(Long compId, UpdateCompilationRequest updateCompilationRequest) {

        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Compilation with id=" + compId + " was not found"));

        if (updateCompilationRequest.getTitle() != null) {
            compilation.setTitle(updateCompilationRequest.getTitle());
        }

        if (updateCompilationRequest.getPinned() != null) {
            compilation.setPinned(updateCompilationRequest.getPinned());
        }

        if (updateCompilationRequest.getEvents() != null) {
            compilation.setEvents(
                    loadEvents(updateCompilationRequest.getEvents())
            );
        }

        return compilationMapper.toCompilationDto(compilationRepository.save(compilation));
    }

    @Override
    public void delete(Long compId) {

        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException(
                    "Compilation with id=" + compId + " was not found");
        }

        compilationRepository.deleteById(compId);
    }

    private Set<Event> loadEvents(List<Long> ids) {

        if (ids == null || ids.isEmpty()) {
            return new HashSet<>();
        }

        List<Event> events = eventsRepository.findAllById(ids);

        if (events.size() != ids.size()) {
            throw new NotFoundException("One or more events were not found");
        }

        return new HashSet<>(events);
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {

        Pageable pageable = PageRequest.of(from / size, size);

        return compilationRepository.findAllByPinned(pinned, pageable)
                .stream()
                .map(compilationMapper::toCompilationDto)
                .toList();
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {

        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException(
                        "Compilation with id=" + compId + " was not found"
                ));

        return compilationMapper.toCompilationDto(compilation);
    }
}
