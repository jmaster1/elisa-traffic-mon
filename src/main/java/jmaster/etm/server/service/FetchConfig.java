package jmaster.etm.server.service;

import jmaster.core.ui.annot.Ui;
import lombok.Data;

import java.util.Map;

/**
 * represents data parsed from "copy as fetch Node.js" from chrome devtools
 */
@Data
@Ui(label = "Fetch config", icon = "key")
public class FetchConfig {

	/**
	 * uri to fetch consumption data from
	 */
	public String uri;

	/**
	 * http headers to send for consumption request
	 */
	public Map<String, String> headers;
}
