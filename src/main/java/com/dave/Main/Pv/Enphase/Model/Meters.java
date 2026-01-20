package com.dave.Main.Pv.Enphase.Model;

public record Meters(
        long last_update,
        int soc,
        int main_relay_state,
        int gen_relay_state,
        int backup_bat_mode,
        int backup_soc,
        int is_split_phase,
        int phase_count,
        int enc_agg_soc,
        int enc_agg_energy,
        int acb_agg_soc,
        int acb_agg_energy,
        PowerInfo pv,
        PowerInfo storage,
        PowerInfo grid,
        PowerInfo load,
        PowerInfo generator
) {
}
