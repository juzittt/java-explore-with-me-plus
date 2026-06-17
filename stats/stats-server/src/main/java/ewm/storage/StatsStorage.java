package ewm.storage;

import ewm.HitDto;
import ewm.StatsDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsStorage {
    void saveHit(HitDto dto);

    List<StatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique);
}
