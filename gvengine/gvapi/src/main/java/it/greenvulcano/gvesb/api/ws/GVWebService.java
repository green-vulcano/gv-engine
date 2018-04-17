package it.greenvulcano.gvesb.api.ws;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import it.greenvulcano.gvesb.api.ws.data.GVWebServicePayload;
import it.greenvulcano.gvesb.buffer.GVException;

@WebService(targetNamespace="http://www.greenvulcano.com/gvesb")
public interface GVWebService {
	
	@WebMethod(operationName = "flow", action="execute")
	GVWebServicePayload execute(@WebParam(name="payload") GVWebServicePayload payload) throws GVException;

}
