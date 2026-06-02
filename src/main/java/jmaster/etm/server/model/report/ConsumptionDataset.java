package jmaster.etm.server.model.report;

import jmaster.etm.server.model.snapshot.ConsumptionSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ConsumptionDataset
{
	public Long phoneNr;

	public String label;

	public List<Point> data = new ArrayList<>();

	public transient Point lastAddedPoint;

	public transient ConsumptionSnapshot lastSkippedSnapshot;
}
