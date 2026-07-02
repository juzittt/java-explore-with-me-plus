package ewm.location.mapper;

import ewm.location.dto.NewLocationDto;
import ewm.location.dto.UpdateLocationDto;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.mapstruct.*;
import ewm.location.dto.LocationDto;
import ewm.location.model.Location;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LocationMapper {

    GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "center", source = ".", qualifiedByName = "toPoint")
    @Mapping(target = "createdAt")
    Location toEntity(NewLocationDto dto);

    LocationDto toDto(Location location);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "center", source = ".", qualifiedByName = "toPointFromUpdate")
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(UpdateLocationDto dto, @MappingTarget Location location);

    @Named("toPoint")
    default Point toPoint(NewLocationDto dto) {
        if (dto == null || dto.getLon() == null || dto.getLat() == null) {
            return null;
        }
        Point point = GEOMETRY_FACTORY.createPoint(new Coordinate(dto.getLon(), dto.getLat()));
        point.setSRID(4326);
        return point;
    }

    @Named("toPointFromUpdate")
    default Point toPointFromUpdate(UpdateLocationDto dto) {
        if (dto == null || dto.getLon() == null || dto.getLat() == null) {
            return null;
        }
        Point point = GEOMETRY_FACTORY.createPoint(new Coordinate(dto.getLon(), dto.getLat()));
        point.setSRID(4326);
        return point;
    }
}
