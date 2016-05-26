package it.greenvulcano.jmx;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

public class MBeanServerInitializerFactory {

	private static final ConcurrentMap<String, Supplier<MBeanServerInitializer>> suppliers = new ConcurrentHashMap<>();
	
	public static void registerSupplier(String name, Supplier<MBeanServerInitializer> supplier) {
		suppliers.put(name, supplier);
	}
	
	public static void deregisterSupplier(String name) {
		suppliers.remove(name);
	}	
	
	public static MBeanServerInitializer create(String name) throws NoSuchElementException {
		return Optional.ofNullable(suppliers.get(name))
					   .orElseThrow(NoSuchElementException::new)
					   .get();
	}
	
}
