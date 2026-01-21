package com.dave.Main.Pv.Enphase.Model;

public record Counters(
        Integer main_CfgLoad,
        Integer main_CfgChanged,
        Integer main_taskUpdate,
        Integer main_sigHUP,
        Integer MqttClient_publish,
        Integer MqttClient_live_debug,
        Integer MqttClient_respond,
        Integer MqttClient_msgarrvd,
        Integer MqttClient_reconnect,
        Integer MqttClient_create,
        Integer MqttClient_setCallbacks,
        Integer MqttClient_connect,
        Integer MqttClient_subscribe,
        Integer SSL_Keys_Create,
        Integer sc_SendStreamCtrl,
        Integer rest_Status
) {
}
