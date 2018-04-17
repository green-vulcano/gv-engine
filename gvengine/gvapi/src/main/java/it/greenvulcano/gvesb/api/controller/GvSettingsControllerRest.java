package it.greenvulcano.gvesb.api.controller;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.cxf.rs.security.cors.CrossOriginResourceSharing;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.GVConfigurationManager.Authority;
import it.greenvulcano.util.xml.XMLUtils;
import it.greenvulcano.util.xml.XMLUtilsException;

@Path("/settings")
@CrossOriginResourceSharing(allowAllOrigins=true, allowCredentials=true, exposeHeaders={"Content-Type", "Content-Range", "X-Auth-Status"})
public class GvSettingsControllerRest extends BaseControllerRest{
	
	private final static Logger LOG = LoggerFactory.getLogger(GvSettingsControllerRest.class);	
	private final static ReentrantLock LOCK = new ReentrantLock();

	@GET @Path("/") 
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({Authority.ADMINISTRATOR, Authority.MANAGER})
	public Response getAllSettings() {
		
		Response response = null; 
		try {		
			Document gvConfig = XMLConfig.getDocument("GVConfig.xml");		
			byte[] gvConfigData = XMLUtils.serializeDOMToByteArray_S(gvConfig, "UTF-8");
			
			JSONObject gvConfigJson = XML.toJSONObject(new String(gvConfigData));
			
			response = Response.ok(gvConfigJson.toString()).build();
		} catch (Exception e) {
			LOG.error("Failed to retrieve GVConfig.xml",e);
			response = Response.status(Status.INTERNAL_SERVER_ERROR).entity(toJson(e)).build();
		}
		
		return response;
		
	}
	
	@GET @Path("/{group}") 
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({Authority.ADMINISTRATOR, Authority.MANAGER})
	public Response getSettings(@PathParam("group") String groupName) {
		
		Response response = null;
		
		try {
			
			Node settings = XMLConfig.getNode("GVConfig.xml", "/GVConfig/"+groupName);
			if (settings!=null) {
				JSONObject settingsJson = XML.toJSONObject(XMLUtils.serializeDOM_S(settings, "UTF-8"));				
				response = Response.ok(settingsJson.toString()).build();
			} else {
				response = Response.status(Status.NOT_FOUND).build();
			}
			
			
		}catch (Exception e) {
			LOG.error("Failed to retrieve node "+groupName+ " in GVConfig.xml",e);
			response = Response.status(Status.NOT_FOUND).build();
		}
		
		return response;
	}
	
	@PUT @Path("/{group}") 
	@Consumes(MediaType.APPLICATION_JSON)
	@RolesAllowed({Authority.ADMINISTRATOR, Authority.MANAGER})
	public void mergeSettigns(@PathParam("group") String groupName, String settings) {
		
		if  (LOCK.tryLock())  {
			try {
				JSONObject gvConfigEntry = new JSONObject(settings).getJSONObject(groupName);
								
									
				Document gvConfig = Optional.ofNullable(CONFIGURATORS.get(groupName))
											.orElseThrow(()-> new WebApplicationException(Status.NOT_FOUND))
											.apply(gvConfigEntry)
											.orElseThrow(()-> new WebApplicationException(Status.SERVICE_UNAVAILABLE));
				writeDocument(gvConfig);
				
				XMLConfig.reload("GVConfig.xml");
			} catch (JSONException jsonException) {
				throw new WebApplicationException(Status.BAD_REQUEST);
			} catch (XMLConfigException xmlConfigException) {
				LOG.error("Failed to reload  GVConfig.xml", xmlConfigException);
				throw new WebApplicationException(xmlConfigException, Status.SERVICE_UNAVAILABLE);
			} catch (IOException e) {
				LOG.error("Failed to update GreenVulcanoPool in GVConfig.xml",e);
				throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
			} finally {
				LOCK.unlock();
			}
		} else {
			throw new WebApplicationException(new IllegalAccessException("Concurrency conflict"), Status.CONFLICT);
		}
	}	
		
	private final static Function<JSONObject, Optional<Document>> gvPoolConfigurator = (gvPoolManager) -> {
		Object newSettings = Optional.ofNullable(gvPoolManager.opt("GreenVulcanoPool")).orElseGet(JSONArray::new);
				
		JSONArray poolSettings = new JSONArray();
		if (newSettings instanceof JSONArray) {
			poolSettings = (JSONArray) newSettings;
		} else {
			poolSettings.put(newSettings);
		}
		
		//avoid duplicate subsytems
		Map<String, JSONObject> poolSettingsMap = IntStream.range(0, poolSettings.length())
															.mapToObj(poolSettings::getJSONObject)
															.collect(Collectors.toMap(p->p.getString("subsystem"), Function.identity(), (p1, p2) -> p2));
	
		try {
			Document gvConfig = XMLConfig.getDocument("GVConfig.xml");
			Element gvPoolManagerElement = (Element) gvConfig.getElementsByTagName("GVPoolManager").item(0);
			
			//clean current settins
			NodeList currentSettings = XMLUtils.selectNodeList_S(gvPoolManagerElement, "./GreenVulcanoPool");			
			IntStream.range(0, currentSettings.getLength()).mapToObj(currentSettings::item).forEach(gvPoolManagerElement::removeChild);
			
			//remove unnecessary empty nodes
			NodeList emptyNodes =XMLUtils.selectNodeList_S(gvPoolManagerElement, "./text()[normalize-space(.)='']");
			IntStream.range(0, emptyNodes.getLength()).mapToObj(emptyNodes::item).forEach(gvPoolManagerElement::removeChild);
			
			//map new settings
			poolSettingsMap.values().stream()
								.map(gvpool->{
									Element poolEntry = gvConfig.createElement("GreenVulcanoPool");
									for (String k : gvpool.keySet()) {
										poolEntry.setAttribute(k, gvpool.get(k).toString());
									}
									return poolEntry;
								})
								.forEach(gvPoolManagerElement::appendChild);
			return Optional.of(gvConfig);
		} catch (XMLConfigException|XMLUtilsException e) {
			LOG.error("Failed to read GreenVulcanoPool in GVConfig.xml",e);
			return Optional.empty();
		} 
	};
	
	private final static Map<String, Function<JSONObject, Optional<Document>>> CONFIGURATORS = new HashMap<>();
	
	static {
		CONFIGURATORS.put("GVPoolManager", gvPoolConfigurator);
	}
	
	private void writeDocument(Document dom) throws IOException {
		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
		    transformer.setOutputProperty(OutputKeys.INDENT, "yes"); 
		    DOMSource source = new DOMSource(dom);
		    StreamResult file = new StreamResult(Files.newOutputStream(Paths.get(XMLConfig.getBaseConfigPath(), "GVConfig.xml"), StandardOpenOption.TRUNCATE_EXISTING));
		    transformer.transform(source, file);
		} catch (TransformerException e) {
			throw new IOException(e);
		}
	}	

}
