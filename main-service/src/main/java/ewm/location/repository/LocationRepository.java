package ewm.location.repository;

import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ewm.events.model.Event;
import ewm.location.model.Location;

import java.util.List;

public interface LocationRepository extends JpaRepository<Location, Long> {

    @Query(value = """
        SELECT l.* FROM locations l
        WHERE ST_DWithin(l.center, :point, l.radius_meters)
        """, nativeQuery = true)
    List<Location> findLocationsContainingPoint(@Param("point") Point point);

    @Query(value = """
        SELECT e.* FROM events e
        JOIN locations l ON e.location_id = l.id
        WHERE l.id = :locationId
          AND ST_DWithin(
              ST_SetSRID(ST_MakePoint(e.lon, e.lat), 4326)::geography,
              l.center,
              l.radius_meters
          )
        """, nativeQuery = true)
    List<Event> findEventsInLocation(@Param("locationId") Long locationId);
}