package jmaster.etm.server.config;

import jmaster.core.ui.annot.Ui;
import lombok.Data;

@Data
@Ui(label = "Etm preferences", icon = "server")
public class EtmPreferences {

    /**
     * Snapshot retrieval enabled.
     */
    public boolean enabled = true;

    /**
     * Delegate uri for http execution.
     */
    public String delegateUri;

    public String delegateBasicAuthUsername;

    public String delegateBasicAuthPassword;
}
