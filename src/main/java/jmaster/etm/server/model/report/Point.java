package jmaster.etm.server.model.report;

import jmaster.etm.server.model.snapshot.ConsumptionSnapshot;

import java.time.Instant;

public class Point {

	public Instant x;
	
	public float y;
	
	public Point(Instant timestamp, Float usedGb) {
		x = timestamp;
		y = usedGb;
	}

    public Point(ConsumptionSnapshot snapshot) {
		this(snapshot.getTimestamp(), snapshot.getUsedGb());
    }
}
