package it.greenvulcano.gvesb.monitoring.service;

import java.util.Set;

import it.greenvulcano.gvesb.monitoring.model.GVPoolStatus;

public interface GVPoolMonitor {

	Set<GVPoolStatus> getGVPoolStatus ();
}
