package com.dave.Ocpp;

import com.dave.Exception.OcppProtocolException;
import com.dave.Logging.Logger;
import com.dave.State.ChargePoint;
import com.dave.StreamProcessor.StreamProcessor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public abstract class OccpSpec {
    protected final static Logger LOGGER = Logger.INSTANCE;

    protected final StreamProcessor streamProcessor;
    protected final ChargePoint chargePoint;

    public OccpSpec(StreamProcessor streamProcessor, ChargePoint chargePoint) {
        this.streamProcessor = streamProcessor;
        this.chargePoint = chargePoint;
    }

    public void onMsg(String msg) throws OcppProtocolException {
        if (msg.isBlank() || msg.length() <= 2 || !msg.contains(",")) {
            throw new OcppProtocolException("Message received from " + this.chargePoint.getIpAddress() + " is malformed");
        }
        int messageTypeId = Integer.parseInt(msg.substring(1, msg.indexOf(',')).trim());

        switch (messageTypeId) {
            case 2:
                onCall(CallMsg.fromMessage(msg));
                break;
            case 3:
                onCallResult(CallResultMsg.fromMessage(msg));
                break;
            case 4:
                onCallError(CallErrorMsg.fromMessage(msg));
                break;
        }
    }

    private void onCall(CallMsg message) throws OcppProtocolException {
        Method m = Arrays.stream(this.getClass().getDeclaredMethods())
                .filter(method -> method.getName().equals("onCall_" + message.action()))
                .findAny()
                .orElseThrow(() -> new OcppProtocolException("No method declared to handle action: " + message.action()));
        m.setAccessible(true);
        try {
            m.invoke(this, message);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new OcppProtocolException("Could not invoke method: " + m.getName() + ": " + e.getMessage());
        }
    }


    private void onCallResult(CallResultMsg message) {
        // TODO
    }

    private void onCallError(CallErrorMsg message) {
        // TODO
    }

}
