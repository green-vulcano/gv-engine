package it.greenvulcano.util.metadata;

public final class PropertyHandlerServiceListener {
	
	 public void onBind(PropertyHandler propertyHandler) {
	   PropertiesHandler.registerHandler(propertyHandler);    
	 }
	    
     public void onUnbind(PropertyHandler propertyHandler) {
       PropertiesHandler.unregisterHandler(propertyHandler);
     }

}
