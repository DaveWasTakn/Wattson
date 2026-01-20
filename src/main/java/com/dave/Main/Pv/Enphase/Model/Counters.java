package com.dave.Main.Pv.Enphase.Model;

public record Counters(
        int main_CfgLoad,
        int main_CfgChanged,
        int main_taskUpdate,
        int main_sigHUP,
        int MqttClient_publish,
        int MqttClient_live_debug,
        int MqttClient_respond,
        int MqttClient_msgarrvd,
        int MqttClient_reconnect,
        int MqttClient_create,
        int MqttClient_setCallbacks,
        int MqttClient_connect,
        int MqttClient_subscribe,
        int SSL_Keys_Create,
        int sc_SendStreamCtrl,
        int rest_Status
) {
}
