package it.greenvulcano.util.metadata;

import java.util.Optional;

public final class PropertyHandlerServiceListener {
	
	 public void onBind(PropertyHandler propertyHandler) {	 
		 Optional.ofNullable(propertyHandler).ifPresent(PropertiesHandler::registerHandler);	 
	       
	 }
	    
     public void onUnbind(PropertyHandler propertyHandler) {
    	 Optional.ofNullable(propertyHandler).ifPresent(PropertiesHandler::unregisterHandler);
     }

}
