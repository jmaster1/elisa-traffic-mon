package jmaster.etm.server.model.report;

import jmaster.etm.server.model.snapshot.ConsumptionSnapshot;
import jmaster.etm.server.model.PhoneOwner;
import jmaster.etm.server.model.snapshot.ConsumptionSnapshotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConsumptionReportService
{
	
	@Autowired
    ConsumptionSnapshotRepository repository;
	
	public List<ConsumptionSnapshot> list(ConsumptionReportFilter filter)
	{
		PageRequest pageRequest = PageRequest.of(0, filter.resolveMaxPoints(),
				Sort.by(Sort.Order.asc(ConsumptionSnapshot.Fields.timestamp)));
		Page<ConsumptionSnapshot> page = repository.findAll(filter.createSpecification(), pageRequest);
		List<ConsumptionSnapshot> content = page.getContent();
		return content;
	}
	
	public Collection<ConsumptionDataset> getConsumptionDatasets(ConsumptionReportFilter filter) {
		Map<Long, ConsumptionDataset> phoneToDataset = new HashMap<>();
		List<ConsumptionSnapshot> snapshots = list(filter);
		for(ConsumptionSnapshot snapshot : snapshots) {
			Long phoneNr = snapshot.getPhoneNr();
			ConsumptionDataset dataset = phoneToDataset.get(phoneNr);
			if(dataset == null) {
				phoneToDataset.put(phoneNr, dataset = new ConsumptionDataset());
				dataset.phoneNr = phoneNr;
				PhoneOwner owner = PhoneOwner.fromPhone(phoneNr);
				dataset.label = owner != null ? owner.name() : phoneNr.toString();
			}
			Point lastAddedPoint = dataset.lastAddedPoint;
			if(lastAddedPoint == null || lastAddedPoint.y != snapshot.getUsedGb()) {
				dataset.data.add(dataset.lastAddedPoint = new Point(snapshot));
				dataset.lastSkippedSnapshot = null;
			} else {
				dataset.lastSkippedSnapshot = snapshot;
			}
		}
		for(ConsumptionDataset dataset : phoneToDataset.values()) {
			if(dataset.lastSkippedSnapshot != null) {
				dataset.data.add(dataset.lastAddedPoint = new Point(dataset.lastSkippedSnapshot));
			}
		}
		return phoneToDataset.values();
	}
}
