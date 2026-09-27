package jmaster.etm.server.model.snapshot.provider;

import jmaster.etm.server.model.PhoneOwner;
import jmaster.etm.server.model.snapshot.FetchConfig;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Retrieves consumption data in a provider-specific format.
 */
public interface ConsumptionProvider {

    String getName();

    /**
     * @param source copied fetch request or a stored request URI
     * @return whether this provider can handle the source
     */
    boolean supports(String source);

    FetchConfig parseFetchConfig(String fetch, FetchConfig currentConfig);

    Map<PhoneOwner, BigDecimal> queryUsedGbByOwner(FetchConfig fetchConfig);
}
