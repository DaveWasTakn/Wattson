package com.dave.Main.Pv.Measurements;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface PvMeasurementsRepository extends CrudRepository<PvMeasurement, Instant> {

    // TODO use native sql queries for timescale functionality

    /*
    eg:
    SELECT time_bucket('1 minute', timestamp) AS minute,
          AVG(current_watt_production) AS avg_prod,
          MAX(current_watt_production) AS peak_prod,
          AVG(grid_watt_power) AS avg_grid
        FROM pv_measurement
        GROUP BY minute
        ORDER BY minute;
     */

}
