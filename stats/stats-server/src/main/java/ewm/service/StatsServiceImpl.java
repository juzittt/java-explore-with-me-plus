package ewm.service;

import ewm.HitDto;
import ewm.StatsDto;
import ewm.storage.StatsStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class StatsServiceImpl implements StatsService {
    private static final String LOG_SAVE_HIT = "сохранен запрос к эндпоинту: {}";
    private static final String LOG_GET_STATS = "получение статистики с {} по {} с адресами {} и уникальностью {}";

    @Autowired
    private StatsStorage statsStorage;

    private static final Logger log = LoggerFactory.getLogger(StatsServiceImpl.class);

    @Override
    public void saveHit(HitDto hit) {
        log.info(LOG_SAVE_HIT, hit);
        statsStorage.saveHit(hit);
    }

    @Override
    public List<StatsDto> getStats(
            LocalDateTime start,
            LocalDateTime end,
            List<String> uris,
            boolean unique
    ) {
        log.info(LOG_GET_STATS, start, end, uris, unique);
        return statsStorage.getStats(start, end, uris, unique);
    }
}
