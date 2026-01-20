package com.dave.Main.Pv.Enphase.Model;

public record EnphaseLiveDataStatus(
        Connection connection,
        Meters meters,
        Tasks tasks,
        Counters counters
) {
}
