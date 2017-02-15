package it.greenvulcano.gvesb.console.api.utility;

import it.greenvulcano.gvesb.console.api.utility.json.JsonUtility.XMLGregorianCalendarTypeAdapter;
import it.greenvulcano.gvesb.console.api.utility.json.ListEntry;
import it.greenvulcano.gvesb.console.api.utility.json.ListWrapper;
import it.greenvulcano.gvesb.console.api.utility.json.MapEntry;
import it.greenvulcano.gvesb.console.api.utility.json.MapWrapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.Expose;


public class Utility {
	
	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(Utility.class);
	private Utility(){

	}

	public static boolean isNull(Object obj){
		return ( obj == null );
	}

	public static boolean isEmpty(String str) {
		return (str == null || str.trim().length() == 0 );
	}	


	public static boolean isEmpty(BigInteger bigInt) {
		return (bigInt == null || bigInt.bitLength() == 0 );
	}	


	/**
	 * 
	 * @param intObj
	 * @return
	 */
	public static boolean isEmpty(Integer intObj) {
		return (intObj == null || intObj.intValue() ==  0 );
	}	


	public static String booleanToString(boolean b){
		return String.valueOf(b);
	}

	@SuppressWarnings("rawtypes")
	public static boolean isListEmpty(List list) {
		return (list == null || list.isEmpty());
	}


	/**
	 * From json.
	 *
	 * @param json the json
	 * @param clazz the clazz
	 * @return the object
	 * @throws ClassNotFoundException the class not found exception
	 */
	public static Object fromJson(String json, Class<?> clazz)
			throws ClassNotFoundException
	{
		Gson gson = getGson();
		return gson.fromJson(json, clazz);
	}

	/**
	 * Gets the gson.
	 *
	 * @return the gson
	 */
	public static Gson getGson()
	{
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(XMLGregorianCalendar.class, new XMLGregorianCalendarTypeAdapter());
		return gsonBuilder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
	}


	public static List<?> fromJsonList(String json, Class<?> clazz) throws JsonParseException, JsonMappingException, IOException{

		ObjectMapper mapper = new ObjectMapper();
//		List<Object> list = mapper.readValue( json, TypeFactory.defaultInstance().constructCollectionType(List.class, clazz) );

		return mapper.readValue( json, TypeFactory.defaultInstance().constructCollectionType(List.class, clazz) );



	}
	
	
	/**
	 * To json.
	 *
	 * @param object the object
	 * @return the string
	 */
	public static String toJson(Object object)
	{
		return toJson(object, true);
	}

	/**
	 * To json.
	 *
	 * @param object the object
	 * @param bAddClassName the b add class name
	 * @return the string
	 */
	public static String toJson(Object object, boolean bAddClassName)
	{
		LOGGER.info("toJson [{}]", object);

		if ((object instanceof MapWrapper))
		{
			LOGGER.info("MapWrapper object detected");

			String json = mapWrapperToJson((MapWrapper)object).toString();
			LOGGER.info("json [{}]", json);
			return json;
		}

		if ((object instanceof ListWrapper))
		{
			LOGGER.info("ListWrapper object detected");
			String json = listWrapperToJson((ListWrapper)object).toString();
			LOGGER.info("json [{}]", json);
			return json;
		}

		Gson gson = getGson(object);

		JsonObject jsonObject = new JsonObject();
		JsonParser parser = new JsonParser();

		String json = gson.toJson(object);
		LOGGER.info("Regular object detected, json [{}]", json);

		if (bAddClassName) {
			jsonObject.add(object.getClass().getSimpleName(), parser.parse(json));

			json = jsonObject.toString();
			LOGGER.info("json [{}]", json);
		}

		return json;
	}
	
	/**
	 * Map wrapper to json.
	 *
	 * @param mapWrapper the map wrapper
	 * @return the json object
	 */
	private static JsonObject mapWrapperToJson(MapWrapper mapWrapper)
	{
		LOGGER.debug("mapWrapperToJson [{}]", mapWrapper);

		JsonObject mapWrapperJson = new JsonObject();

		JsonArray mapWrapperElements = new JsonArray();
		for (MapEntry entry : mapWrapper.getEntryList())
		{
			LOGGER.debug("MapEntry [{}]", entry);

			if (entry.getValue().getClass().equals(MapWrapper.class))
			{
				LOGGER.debug("MapEntry value is a MapWrapper");
				JsonObject entryJson = new JsonObject();
				entryJson.add(entry.getKey().toString(), mapWrapperToJson((MapWrapper)entry.getValue()));
				mapWrapperElements.add(entryJson);
			}
			else if (entry.getValue().getClass().equals(ListWrapper.class))
			{
				LOGGER.debug("MapEntry value is a ListWrapper");
				JsonObject entryJson = new JsonObject();
				entryJson.add(entry.getKey().toString(), listWrapperToJson((ListWrapper)entry.getValue()));
				mapWrapperElements.add(entryJson);
			}
			else {
				LOGGER.debug("MapEntry value is a regular object");

				Gson gson = getGson();
				JsonObject entryJson = new JsonObject();
				JsonObject objectJson = new JsonObject();
				objectJson.add(entry.getValue().getClass().getName(), gson.toJsonTree(entry.getValue()));
				entryJson.add(entry.getKey().toString(), objectJson);
				mapWrapperElements.add(entryJson);
			}

		}

		mapWrapperJson.add(MapWrapper.class.getName(), mapWrapperElements);

		return mapWrapperJson;
	}
	
	/**
	 * List wrapper to json.
	 *
	 * @param listWrapper the list wrapper
	 * @return the json object
	 */
	private static JsonObject listWrapperToJson(ListWrapper listWrapper)
	{
		LOGGER.debug("listWrapperToJson [{}]", listWrapper);

		JsonObject listWrapperJson = new JsonObject();

		JsonArray listWrapperElements = new JsonArray();
		for (ListEntry entry : listWrapper.getEntryList())
		{
			LOGGER.debug("ListEntry [{}]", entry);

			if (entry.getValue().getClass().equals(MapWrapper.class)) {
				LOGGER.debug("ListEntry value is a MapWrapper");
				listWrapperElements.add(mapWrapperToJson((MapWrapper)entry.getValue()));
			}
			else if (entry.getValue().getClass().equals(ListWrapper.class))
			{
				LOGGER.debug("ListEntry value is a ListWrapper");
				listWrapperElements.add(listWrapperToJson((ListWrapper)entry.getValue()));
			}
			else
			{
				LOGGER.debug("ListEntry value is a regular object");

				Gson gson = getGson();
				JsonObject objectJson = new JsonObject();
				objectJson.add(entry.getValue().getClass().getName(), gson.toJsonTree(entry.getValue()));
				listWrapperElements.add(objectJson);
			}

		}

		listWrapperJson.add(ListWrapper.class.getName(), listWrapperElements);

		return listWrapperJson;
	}
	
	
	/**
	 * Gets the gson.
	 *
	 * @param object the object
	 * @return the gson
	 */
	public static Gson getGson(Object object)
	{
		Field[] fields = object.getClass().getDeclaredFields();

		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(XMLGregorianCalendar.class, new XMLGregorianCalendarTypeAdapter());

		Gson gson = null;
		for (Field field : fields) {
			Annotation[] annotations = field.getDeclaredAnnotations();

			for (Annotation annotation : annotations) {
				if ((annotation instanceof Expose)) {
					gson = gsonBuilder.excludeFieldsWithoutExposeAnnotation().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
					return gson;
				}
			}
		}

		gson = gsonBuilder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
		return gson;
	}
	
	
	/**
	 * Convert stream to string.
	 *
	 * @param is the is
	 * @return the string
	 * @throws IOException 
	 * @ the exception
	 */
	public static String convertStreamToString(InputStream is) throws IOException  {

		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
			sb.append(line + "\n");
		}
		is.close();
		String x = sb.toString();
		return x;
		
	}
	
}

