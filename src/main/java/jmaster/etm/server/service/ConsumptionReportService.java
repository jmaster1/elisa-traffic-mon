package jmaster.etm.server.service;

import com.turkraft.springfilter.converter.FilterSpecification;
import jmaster.etm.server.model.ConsumptionDataset;
import jmaster.etm.server.model.ConsumptionSnapshot;
import jmaster.etm.server.model.PhoneOwner;
import jmaster.etm.server.model.Point;
import jmaster.etm.server.repository.ConsumptionSnapshotRepository;
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
	
	public List<ConsumptionSnapshot> list(FilterSpecification<ConsumptionSnapshot> filter)
	{
		PageRequest pageRequest = PageRequest.of(0, 10000,
				Sort.by(Sort.Order.asc(ConsumptionSnapshot.Fields.timestamp)));
		Page<ConsumptionSnapshot> page = repository.findAll(filter, pageRequest);
		List<ConsumptionSnapshot> content = page.getContent();
		return content;
	}
	
	public Collection<ConsumptionDataset> getConsumptionDatasets(FilterSpecification<ConsumptionSnapshot> filter) {
		Map<Long, ConsumptionDataset> phoneToDataset = new HashMap<>();
		List<ConsumptionSnapshot> snapshots = list(filter);
		for(ConsumptionSnapshot snapshot : snapshots) {
			Long phoneNr = snapshot.getPhoneNr();
			ConsumptionDataset dataset = phoneToDataset.get(phoneNr);
			if(dataset == null) {
				phoneToDataset.put(phoneNr, dataset = new ConsumptionDataset());
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
