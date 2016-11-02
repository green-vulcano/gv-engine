package it.greenvulcano.gvesb.api.ws.soap;

import java.util.Map.Entry;
import java.util.Objects;

import javax.xml.ws.WebServiceContext;

import it.greenvulcano.gvesb.api.security.JaxWsIdentityInfo;
import it.greenvulcano.gvesb.api.ws.GVWebService;
import it.greenvulcano.gvesb.api.ws.data.GVWebServicePayload;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVException;
import it.greenvulcano.gvesb.core.exc.GVCoreException;
import it.greenvulcano.gvesb.core.pool.GreenVulcanoPool;
import it.greenvulcano.gvesb.core.pool.GreenVulcanoPoolManager;
import it.greenvulcano.gvesb.identity.GVIdentityHelper;

public class GVWebServiceSoap implements GVWebService {
	
	WebServiceContext webServiceContext;
	
	@Override
	public GVWebServicePayload execute(GVWebServicePayload requestPayload) throws GVException {
				
		
		GVIdentityHelper.push(new JaxWsIdentityInfo(webServiceContext));
		
		GVBuffer inputBuffer = new GVBuffer();
		
		if (Objects.nonNull(requestPayload.getProperties())){
			for (Entry<String, String> e : requestPayload.getProperties().entrySet()) {
				inputBuffer.setProperty(e.getKey(), e.getValue());
			}
		}
							
		inputBuffer.setService(requestPayload.getService());
		inputBuffer.setObject(requestPayload.getData());
		
		GVWebServicePayload responsePayload = null; 
		try {
		
			GreenVulcanoPool gvpoolInstance = GreenVulcanoPoolManager.instance().getGreenVulcanoPool("gvapi");
			GVBuffer outputBuffer = gvpoolInstance.forward(inputBuffer, requestPayload.getOperation());
			
			responsePayload = new GVWebServicePayload();
			outputBuffer.getPropertyNamesSet().stream().forEach(p -> requestPayload.getProperties().put(p, outputBuffer.getProperty(p)));
			responsePayload.setData(outputBuffer.getObject()+"");
		} catch (Exception e) {
			throw new GVCoreException(null, e);
		}
		
		return responsePayload;
	}
	
}
