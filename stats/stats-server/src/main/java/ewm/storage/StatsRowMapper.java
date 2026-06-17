package ewm.storage;

import ewm.StatsDto;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class StatsRowMapper implements RowMapper<StatsDto> {
    @Override
    public StatsDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        return StatsDto.builder()
            .app(rs.getString("app"))
            .uri(rs.getString("uri"))
            .hits(rs.getLong("hits")).build();
    }
}
