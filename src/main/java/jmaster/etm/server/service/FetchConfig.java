package jmaster.etm.server.service;

import jmaster.core.ui.annot.Ui;
import lombok.Data;

import java.util.Map;

/**
 * represents data parsed from "copy as fetch Node.js" from chrome devtools
 */
@Data
@Ui(label = "Etm preferences", icon = "server")
public class FetchConfig {

	/**
	 * snapshot retrieval enabled
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

	/**
	 * delegate uri for http execution
	 */
	public String delegateUri;

	public String delegateBasicAuthUsername;

	public String delegateBasicAuthPassword;
}
