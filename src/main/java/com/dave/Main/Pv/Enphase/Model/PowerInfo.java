package com.dave.Main.Pv.Enphase.Model;

public record PowerInfo(
        long agg_p_mw,
        long agg_s_mva,
        long agg_p_ph_a_mw,
        long agg_p_ph_b_mw,
        long agg_p_ph_c_mw,
        long agg_s_ph_a_mva,
        long agg_s_ph_b_mva,
        long agg_s_ph_c_mva
) {
}
