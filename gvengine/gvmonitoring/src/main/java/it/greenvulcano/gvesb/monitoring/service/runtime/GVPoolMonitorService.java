package it.greenvulcano.gvesb.monitoring.service.runtime;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import it.greenvulcano.gvesb.core.pool.GreenVulcanoPool;
import it.greenvulcano.gvesb.core.pool.GreenVulcanoPoolManager;
import it.greenvulcano.gvesb.monitoring.model.GVPoolStatus;
import it.greenvulcano.gvesb.monitoring.service.GVPoolMonitor;

public class GVPoolMonitorService implements GVPoolMonitor {
	@Override
	public Set<GVPoolStatus> getGVPoolStatus() {

		Set<String> poolNames = GreenVulcanoPoolManager.instance().getActivePoolNames();
		return (Set<GVPoolStatus>) poolNames.stream()
				.map(GreenVulcanoPoolManager.instance()::getGreenVulcanoPool)
				.map(gvpool -> {
					new GVPoolStatus(Instant.now(), gvpool.getSubsystem(), gvpool.getMaximumSize(),
						gvpool.getInUseCount());
					})
				.collect(Collectors.toSet());
	}

}
