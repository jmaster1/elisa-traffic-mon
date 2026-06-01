package jmaster.etm.server.model;

import java.util.ArrayList;
import java.util.List;

public class ConsumptionDataset
{
	public String label;

	public List<Point> data = new ArrayList<>();

	public transient Point lastAddedPoint;

	public transient ConsumptionSnapshot lastSkippedSnapshot;
}
