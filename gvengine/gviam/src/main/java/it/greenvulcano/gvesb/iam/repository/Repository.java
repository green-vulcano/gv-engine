package it.greenvulcano.gvesb.iam.repository;

import java.io.Serializable;
import java.util.Optional;
import java.util.Set;

public interface Repository<T, K extends Serializable> {
	
	Optional<T> get(K key);
	
	void add(T role);
	
	Set<T> getAll();
	
	void remove(T role);

}
