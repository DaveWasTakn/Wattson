package com.dave.Main.Pv.Enphase.Model;

public record Connection(
        String mqtt_state,
        String prov_state,
        String auth_state,
        String sc_stream,
        String sc_debug
) {
}
