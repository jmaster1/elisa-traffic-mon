package jmaster.etm.server.model.report;

import lombok.Data;

@Data
public class MonthlyConsumptionProgress {

    public Long phoneNr;

    public String label;

    public String color;

    public String usedGbText;

    public int quotaGb;

    public int percent;

    public int cappedPercent;
}
