package com.dave.Main.Pv.Enphase.Model;

public record Meters(
        Long last_update,
        Integer soc,
        Integer main_relay_state,
        Integer gen_relay_state,
        Integer backup_bat_mode,
        Integer backup_soc,
        Integer is_split_phase,
        Integer phase_count,
        Integer enc_agg_soc,
        Integer enc_agg_energy,
        Integer acb_agg_soc,
        Integer acb_agg_energy,
        PowerInfo pv,
        PowerInfo storage,
        PowerInfo grid,
        PowerInfo load,
        PowerInfo generator
) {
}
