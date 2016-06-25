package it.greenvulcano.gvesb.api;

public interface GvInstanceController<T> {
	
	T bind(String key);
	
	T unbind();

}
