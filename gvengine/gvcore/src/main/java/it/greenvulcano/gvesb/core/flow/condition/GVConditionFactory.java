package it.greenvulcano.gvesb.core.flow.condition;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Supplier;

import it.greenvulcano.gvesb.identity.condition.IdentityCondition;
import it.greenvulcano.gvesb.internal.condition.ExceptionCondition;
import it.greenvulcano.gvesb.internal.condition.GVBufferCondition;
import it.greenvulcano.gvesb.internal.condition.GVCondition;
import it.greenvulcano.gvesb.internal.condition.ScriptCondition;

/**
 * A static factory of know GVCondition implementations 
 * mapped by class name
 *
 *
 */
public final class GVConditionFactory {
	
	private final static Map<String, Supplier<GVCondition>> suppliers;
	
	static {
		suppliers = new HashMap<>();
		suppliers.put("ScriptCondition", ScriptCondition::new);
		suppliers.put("GVBufferCondition", GVBufferCondition::new);
		suppliers.put("ExceptionCondition", ExceptionCondition::new);
		suppliers.put("IdentityCondition", IdentityCondition::new);
	}
	
	private GVConditionFactory(){}
	
	/**
	 * Create a GVCondition using the implementation identified by 
	 * the specified {@code coditionName}
	 * 
	 * @param conditionName The implementation identifier
	 * @return An GVCondition instance
	 * 
	 * @throws NoSuchElementException Specified implementation not found  
	 *  
	 */	
	public static GVCondition make(String conditionName) {
		return Optional.ofNullable(suppliers.get(conditionName))
					   .orElseThrow(NoSuchElementException::new)
					   .get();
	}
	

}
