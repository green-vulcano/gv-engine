package it.greenvulcano.gvesb.iam.repository;

import java.io.Serializable;
import java.util.Optional;
import java.util.Set;

/**
 * 
 * Generic business interface to deal with domain entities
 * 
 */
public interface Repository<T, K extends Serializable> {
	
	Optional<T> get(K key);
	
	void add(T entity);
	
	Set<T> getAll();
	
	void remove(T entity);

}
