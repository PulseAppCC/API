package cc.pulseapp.api.service;

import cc.pulseapp.api.model.Feature;
import com.flagsmith.FlagsmithClient;
import com.flagsmith.models.BaseFlag;
import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * This service is responsible for
 * fetching remote feature flags.
 *
 * @author Braydon
 */
@Service @Log4j2(topic = "Flags")
public final class FlagsService {
    private static final long FETCH_INTERVAL = TimeUnit.SECONDS.toMillis(30L);

    @Value("${flagsmith.api-url}")
    private String apiUrl;

    @Value("${flagsmith.api-key}")
    private String apiKey;

    /**
     * The Flagsmith client.
     */
    private FlagsmithClient client;

    @PostConstruct
    public void onInitialize() {
        client = FlagsmithClient.newBuilder()
                .withApiUrl(apiUrl)
                .setApiKey(apiKey)
                .build();

        // Schedule a task to fetch all flags
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override @SneakyThrows
            public void run() {
                int oldFlags = Feature.hash();
                for (BaseFlag flag : client.getEnvironmentFlags().getAllFlags()) {
                    Feature feature = Feature.getById(flag.getFeatureName());
                    if (feature == null) {
                        continue;
                    }
                    Object value = flag.getValue();
                    feature.setEnabled(flag.getEnabled());
                    feature.setValue(value instanceof String stringedValue && (stringedValue.isBlank()) ? null : value);
                }
                if (oldFlags != Feature.hash()) {
                    log.info("Fetched new flags (:");
                }
            }
        }, 0L, FETCH_INTERVAL);
    }
}