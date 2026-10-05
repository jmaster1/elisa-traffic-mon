package jmaster.etm.server.model.snapshot;

import jmaster.etm.server.model.PhoneOwner;
import jmaster.etm.server.model.snapshot.provider.ConsumptionProvider;
import jmaster.system.log.error.ErrorLogService;
import jmaster.system.prefs.PrefsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * reads consumption snapshots from web and writes them into db
 */
@Service
public class ConsumptionRegisterService {

    Logger logger = LoggerFactory.getLogger(ConsumptionRegisterService.class);

    @Autowired
    ConsumptionSnapshotRepository repository;

    @Autowired
    ErrorLogService errorLogService;

    @Autowired
    PrefsService prefsService;

    @Autowired
    List<ConsumptionProvider> consumptionProviders;

    @Autowired
    ApplicationEventPublisher eventPublisher;

    private Exception lastError;

    private Date lastErrorDate;

    public FetchConfig parseFetchConfig(String fetch) {
        return getProvider(fetch).parseFetchConfig(fetch, getFetchConfig());
    }

    public String getProviderName(FetchConfig fetchConfig) {
        return getProvider(fetchConfig.uri).getName();
    }

    @Scheduled(cron = "0 */10 * * * *")
    public void queryConsumptionSnapshots() {
        clearLastError();
        try {
            FetchConfig fetchConfig = prefsService.getPrefs(FetchConfig.class);
            if (fetchConfig != null && fetchConfig.uri != null && fetchConfig.enabled) {
                queryAndSaveSnapshots(fetchConfig);
            }
        } catch (Exception ex) {
            logger.error("queryConsumptionSnapshots() failed", ex);
            errorLogService.create(ex, null);
            lastError = ex;
            lastErrorDate = new Date();
        }
    }

    public Map<PhoneOwner, BigDecimal> queryAndSaveCurrentSnapshots() {
        FetchConfig fetchConfig = prefsService.getPrefs(FetchConfig.class);
        if (fetchConfig == null || fetchConfig.uri == null || fetchConfig.uri.isBlank()) {
            throw new IllegalStateException("Consumption fetch configuration is missing");
        }
        Map<PhoneOwner, BigDecimal> result = queryAndSaveSnapshots(fetchConfig);
        clearLastError();
        return result;
    }

    private Map<PhoneOwner, BigDecimal> queryAndSaveSnapshots(FetchConfig fetchConfig) {
        ConsumptionProvider provider = getProvider(fetchConfig.uri);
        Map<PhoneOwner, BigDecimal> usedGbByOwner;
        try {
            usedGbByOwner = provider.queryUsedGbByOwner(fetchConfig);
        } catch (RuntimeException ex) {
            if (isTeliaConfig(fetchConfig) && isStatus440(ex)) {
                logger.warn("Telia provider received HTTP 440; requesting an automatic browser login flow");
                eventPublisher.publishEvent(new TeliaSessionRefreshRequestedEvent(
                        "Telia provider received Bad status: 440."));
            }
            throw ex;
        }
        Instant timestamp = Instant.now();
        for (Map.Entry<PhoneOwner, BigDecimal> entry : usedGbByOwner.entrySet()) {
            ConsumptionSnapshot snapshot = new ConsumptionSnapshot();
            snapshot.setTimestamp(timestamp);
            snapshot.setPhoneNr(entry.getKey().phoneNr);
            snapshot.setUsedGb(entry.getValue().floatValue());
            repository.save(snapshot);
        }
        return usedGbByOwner;
    }

    private boolean isTeliaConfig(FetchConfig fetchConfig) {
        return fetchConfig.uri != null && fetchConfig.uri.contains("iseteenindus.telia.ee");
    }

    private boolean isStatus440(Throwable exception) {
        Throwable current = exception;
        while (current != null) {
            if (current.getMessage() != null && current.getMessage().contains("Bad status: 440")) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private ConsumptionProvider getProvider(String source) {
        return consumptionProviders.stream()
                .filter(provider -> provider.supports(source))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unsupported consumption provider"
                ));
    }

    public FetchConfig getFetchConfig() {
        return prefsService.getPrefs(FetchConfig.class);
    }

    public FetchConfig saveFetchConfig(FetchConfig fetchConfig) {
        prefsService.savePrefs(fetchConfig);
        return fetchConfig;
    }

    public LastError getLastError() {
        if (lastError == null) {
            return null;
        }
        LastError ret = new LastError();
        ret.message = getErrorMessage(lastError);
        ret.date = lastErrorDate;
        return ret;
    }

    private String getErrorMessage(Exception exception) {
        Throwable current = exception;
        while (current != null) {
            String message = current.getMessage();
            if (message != null && !message.isBlank()) {
                return current.getClass().getSimpleName() + ": " + message;
            }
            current = current.getCause();
        }
        return exception.getClass().getSimpleName();
    }

    public void clearLastError() {
        lastError = null;
        lastErrorDate = null;
    }
}
