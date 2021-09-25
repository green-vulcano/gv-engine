package it.greenvulcano.util.metadata.properties;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.greenvulcano.gvesb.iam.service.mongodb.GVPropertiesManager;
import it.greenvulcano.util.metadata.PropertiesHandlerException;
import it.greenvulcano.util.metadata.PropertyHandler;


public class MongoDbStorePropertiesHandler implements PropertyHandler {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    private GVPropertiesManager propertiesManager;
    
    public void setPropertiesManager(GVPropertiesManager propertiesManager) {
        this.propertiesManager = propertiesManager;
    }
    
    @Override
    public String expand(String type, String str, Map<String, Object> inProperties, Object object, Object extra) throws PropertiesHandlerException {
        
        try {
            
            return propertiesManager.retrieve(str).orElseThrow().getValue();
        
        } catch (NoSuchElementException e) {           
            LOG.error("ERROR MongoDB store - key not found: "+str);
            
        } catch (Exception e) {
            LOG.error("ERROR MongoDB store - failed to retrieve key: "+str, e);
        }
        
        
        return "gvstore" + PROP_START + str + PROP_END;
    }

    @Override
    public List<String> getManagedTypes() {
        return Collections.unmodifiableList(Arrays.asList("gvstore"));
    }
    
    @Override
    public void cleanupResources() {
    	// do nothing
    }

}
