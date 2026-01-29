package com.dave.Main.Pv.Enphase;

import com.dave.Main.Exception.MissingConfigurationException;
import com.dave.Main.Pv.Enphase.Model.EnphaseLiveDataStatus;
import com.dave.Main.Pv.PvSystem;
import com.dave.Main.State.Observe.PvSystemEvent;
import com.dave.Main.State.State;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@Service
public class EnphasePvSystem implements PvSystem {
    private static final Logger LOGGER = LoggerFactory.getLogger(EnphasePvSystem.class);

    private final String username;
    private final String password;
    private final String gatewaySerialNumber;
    private final String gatewayIp;

    private String apiToken;

    private final WebClient webClient;
    private final WebClient webClient_allowsSelfSigned;
    private final ApiTokenRepository apiTokenRepository;
    private final ObjectMapper objectMapper;
    private final State state;

    private EnphaseLiveDataStatus status;

    private volatile boolean initialized = false;

    @Autowired
    public EnphasePvSystem(
            @Value("${pv.enphase.enlighten.username}") String username,
            @Value("${pv.enphase.enlighten.password}") String password,
            @Value("${pv.enphase.gateway.serialNumber}") String gatewaySerialNumber,
            @Value("${pv.enphase.gateway.ip}") String gatewayIp,
            WebClient webClient,
            WebClient webClient_allowsSelfSigned,
            ApiTokenRepository apiTokenRepository,
            ObjectMapper objectMapper,
            State state
    ) throws MissingConfigurationException {
        this.username = username;
        this.password = password;
        this.gatewaySerialNumber = gatewaySerialNumber;
        this.gatewayIp = gatewayIp;
        this.webClient = webClient;
        this.webClient_allowsSelfSigned = webClient_allowsSelfSigned;
        this.apiTokenRepository = apiTokenRepository;
        this.objectMapper = objectMapper;
        this.state = state;
    }

    @EventListener(ApplicationReadyEvent.class)
    private void init() throws MissingConfigurationException {
        if (gatewaySerialNumber == null || username == null || password == null || gatewayIp == null) {
            throw new MissingConfigurationException("Missing either 'gatewaySerialNumber' or 'username' or 'password' or 'gatewayIp' configuration in application.properties");
        }
        Optional<ApiToken> apiToken = this.apiTokenRepository.findFirstByOrderByIdDesc();
        if (apiToken.isPresent()) {
            LOGGER.info("Found existing API token");
            this.apiToken = apiToken.get().getToken();
        } else {
            LOGGER.warn("Fetching new API token");
            getAndSaveNewApiToken();
        }
        initialized = true;
    }

    @Scheduled(fixedDelayString = "${pv.poll.interval_ms:5000}")
    public void poll() {
        if (!initialized) {
            return;
        }
        try {
            this.updateStatus();
        } catch (Exception e) {
            LOGGER.error("Failed to poll Enphase status", e);
        }
    }

    private void getAndSaveNewApiToken() { // TODO also call if 401 somewhere !!
        String newToken = this.getApiToken();
        this.apiTokenRepository.save(new ApiToken(newToken, Instant.now()));
        this.apiToken = newToken;
    }

    private void updateStatus() {
        this.status = this.requestLiveDataStatus();
        this.state.publish(new PvSystemEvent());
    }

    /// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public int getCurrentWattProduction() {
        return mwToW(this.status.meters().pv().agg_p_mw());
    }

    @Override
    public int getCurrentWattConsumption() {
        return mwToW(this.status.meters().load().agg_p_mw());
    }

    @Override
    public int getBatteryWattPower() {
        return mwToW(this.status.meters().storage().agg_p_mw());
    }

    @Override
    public int getGridWattPower() {
        return mwToW(this.status.meters().grid().agg_p_mw());
    }

    @Override
    public int getBatterySocPercent() {
        return this.status.meters().soc();
    }

    @Override
    public long getLastMeterUpdateEpoch() {
        return this.status.meters().last_update();
    }

    private static int mwToW(Long mw) {
        return Math.toIntExact(mw / 1000);
    }

    /// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private EnphaseLiveDataStatus requestLiveDataStatus() {
        String response = this.webClient_allowsSelfSigned.get()
                .uri("https://" + gatewayIp + "/ivp/livedata/status")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + this.apiToken)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return objectMapper.readValue(response, EnphaseLiveDataStatus.class);
    }

    private String getApiToken() {
        // LOGIN
        SessionResponse sessionResponse = webClient.post()
                .uri("https://enlighten.enphaseenergy.com/login/login.json")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("user[email]", this.username)
                        .with("user[password]", this.password))
                .retrieve()
                .bodyToMono(SessionResponse.class)
                .block();

        if (sessionResponse == null || sessionResponse.sessionId == null) {
            throw new RuntimeException("Failed to get session_id");
        }

        // GET TOKEN
        Map<String, String> tokenRequest = Map.of(
                "session_id", sessionResponse.sessionId,
                "serial_num", this.gatewaySerialNumber,
                "username", this.username
        );

        return webClient.post()
                .uri("https://entrez.enphaseenergy.com/tokens")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(tokenRequest)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    static class SessionResponse {
        @JsonProperty("session_id")
        public String sessionId;
    }

}
