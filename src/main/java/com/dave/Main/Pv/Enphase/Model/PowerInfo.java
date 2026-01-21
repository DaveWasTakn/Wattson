package com.dave.Main.Pv.Enphase.Model;

public record PowerInfo(
        Long agg_p_mw,
        Long agg_s_mva,
        Long agg_p_ph_a_mw,
        Long agg_p_ph_b_mw,
        Long agg_p_ph_c_mw,
        Long agg_s_ph_a_mva,
        Long agg_s_ph_b_mva,
        Long agg_s_ph_c_mva
) {
}
