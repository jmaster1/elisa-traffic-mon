package jmaster.etm.server.model.report;

import jmaster.etm.server.model.snapshot.ConsumptionSnapshot;

import java.util.Date;

public class Point
{
	public Date x;
	
	public float y;
	
	public Point(Date timestamp, Float usedGb) {
		x = timestamp;
		y = usedGb;
	}

    public Point(ConsumptionSnapshot snapshot) {
		this(snapshot.getTimestamp(), snapshot.getUsedGb());
    }
}
