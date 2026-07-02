package ewm.location.service;

import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ewm.events.dto.EventShortDto;
import ewm.events.mapper.EventMapper;
import ewm.exception.NotFoundException;
import ewm.location.dto.LocationDto;
import ewm.location.dto.NewLocationDto;
import ewm.location.dto.UpdateLocationDto;
import ewm.location.mapper.LocationMapper;
import ewm.location.model.Location;
import ewm.location.repository.LocationRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;
    private final EventMapper eventMapper;

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Override
    @Transactional
    public LocationDto create(NewLocationDto dto) {
        Location location = locationMapper.toEntity(dto);
        Location saved = locationRepository.save(location);
        return locationMapper.toDto(saved);
    }

    @Override
    public Page<LocationDto> getAll(Pageable pageable) {
        return locationRepository.findAll(pageable).map(locationMapper::toDto);
    }

    @Override
    public LocationDto getById(Long id) {
        Location location = getLocationOrThrow(id);
        return locationMapper.toDto(location);
    }

    @Override
    @Transactional
    public LocationDto update(Long id, UpdateLocationDto dto) {
        Location existing = getLocationOrThrow(id);
        locationMapper.updateEntity(dto, existing);

        if (dto.getName() != null) {
            existing.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            existing.setDescription(dto.getDescription());
        }
        if (dto.getLocationType() != null) {
            existing.setLocationType(dto.getLocationType());
        }
        if (dto.getLat() != null && dto.getLon() != null) {
            existing.setLat(dto.getLat());
            existing.setLon(dto.getLon());
            existing.setCenter(createPoint(dto.getLon(), dto.getLat()));
        }
        if (dto.getRadiusMeters() != null) {
            existing.setRadiusMeters(dto.getRadiusMeters());
        }

        Location updated = locationRepository.save(existing);
        return locationMapper.toDto(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!locationRepository.existsById(id)) {
            throw new NotFoundException("Локация не найдена с id: " + id);
        }
        locationRepository.deleteById(id);
    }

    @Override
    public List<LocationDto> findLocationsContainingPoint(Float lat, Float lon) {
        Point point = createPoint(lon, lat);
        return locationRepository.findLocationsContainingPoint(point)
                .stream()
                .map(locationMapper::toDto)
                .toList();
    }

    @Override
    public List<EventShortDto> findEventsInLocation(Long locationId) {
        if (!locationRepository.existsById(locationId)) {
            throw new NotFoundException("Локация не найдена с id: " + locationId);
        }
        return locationRepository.findEventsInLocation(locationId)
                .stream()
                .map(eventMapper::toEventShortDto)
                .toList();
    }

    private Location getLocationOrThrow(Long id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Локация не найдена с id: " + id));
    }

    private Point createPoint(double lon, double lat) {
        Point point = geometryFactory.createPoint(new Coordinate(lon, lat));
        point.setSRID(4326);
        return point;
    }
}