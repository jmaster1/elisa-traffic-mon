package jmaster.etm.server.model.snapshot;

import jmaster.core.ui.annot.Ui;
import jmaster.system.prefs.PrefsType;
import lombok.Data;

import java.util.Map;

/**
 * Request settings used by consumption providers to retrieve snapshots.
 */
@Data
@Ui(label = "Fetch config", icon = "key")
@PrefsType
public class FetchConfig {

	public int monthlyQuotaGb = 500;
	/**
	 * Snapshot retrieval enabled.
	 */
	public boolean enabled;

	/**
	 * uri to fetch consumption data from
	 */
	public String uri;

	/**
	 * http headers to send for consumption request
	 */
	public Map<String, String> headers;
}
