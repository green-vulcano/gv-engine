package it.greenvulcano.gvesb.core.flow.iteration;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVException;

/**
 * A contract to hadle the iteration logic over a {@link GVBuffer}.
 * 
 * 
 * @version 4.0.0 20160603
 * @author GreenVulcano Developer Team
 * 
 */
public interface LoopController {
	
	/**
	 * Inventory of all {@link LoopController} implementation
	 * 
	 *
	 */
	enum Type { JAVA_COLLECTION("javaCollection"), 
		JSON_OBJECT("jsonObject"), 
		JSON_ARRAY("jsonArray"), 
		XML_NODE("xmlNode");

		private static final Map<String, Type> typesMap;

		static {
			typesMap = Arrays.stream(values()).collect(Collectors.toMap(Type::getId, Function.identity()));			 
		}

		static Optional<Type> getById(String id) {		    
			if (Objects.nonNull(id)) {
				return Optional.ofNullable(typesMap.get(id));
			} else {
				return Optional.empty();
			}

		}

		private final String id;		

		private Type(String id) {
			this.id = id;
		}

		String getId() {
			return id;
		}

	}
	
	GVBuffer executeLoop(GVBuffer inputData, boolean onDebug) throws GVException;

}
