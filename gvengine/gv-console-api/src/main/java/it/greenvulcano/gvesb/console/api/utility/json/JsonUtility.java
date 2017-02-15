package it.greenvulcano.gvesb.console.api.utility.json;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.annotations.Expose;


public class JsonUtility {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(JsonUtility.class);
	
	private static HashMap<String, Class> classesNotToMarshal = new HashMap();

	public static String toJson(Object object) {
		return toJson(object, true);
	}

	public static String toJson(Object object, boolean bAddClassName) {
		LOGGER.info("toJson [{}]", object);

		if (object instanceof MapWrapper) {
			LOGGER.info("MapWrapper object detected");

			String json = mapWrapperToJson((MapWrapper) object).toString();
			LOGGER.info("json [{}]", json);
			return json;
		}

		if (object instanceof ListWrapper) {
			LOGGER.info("ListWrapper object detected");
			String json = listWrapperToJson((ListWrapper) object).toString();
			LOGGER.info("json [{}]", json);
			return json;
		}

		Gson gson = getGson(object);

		JsonObject jsonObject = new JsonObject();
		JsonParser parser = new JsonParser();

		String json = gson.toJson(object);
		LOGGER.info("Regular object detected, json [{}]", json);

		if (bAddClassName) {
			jsonObject.add(object.getClass().getSimpleName(),
					parser.parse(json));

			json = jsonObject.toString();
			LOGGER.info("json [{}]", json);
		}

		return json;
	}

	private static JsonObject mapWrapperToJson(MapWrapper mapWrapper) {
		LOGGER.debug("mapWrapperToJson [{}]", mapWrapper);

		JsonObject mapWrapperJson = new JsonObject();

		JsonArray mapWrapperElements = new JsonArray();
		for (MapEntry entry : mapWrapper.getEntryList()) {
			LOGGER.debug("MapEntry [{}]", entry);

			if (entry.getValue().getClass().equals(MapWrapper.class)) {
				LOGGER.debug("MapEntry value is a MapWrapper");
				JsonObject entryJson = new JsonObject();
				entryJson.add(entry.getKey().toString(),
						mapWrapperToJson((MapWrapper) entry.getValue()));
				mapWrapperElements.add(entryJson);
			} else if (entry.getValue().getClass().equals(ListWrapper.class)) {
				LOGGER.debug("MapEntry value is a ListWrapper");
				JsonObject entryJson = new JsonObject();
				entryJson.add(entry.getKey().toString(),
						listWrapperToJson((ListWrapper) entry.getValue()));
				mapWrapperElements.add(entryJson);
			} else {
				LOGGER.debug("MapEntry value is a regular object");

				Gson gson = getGson();
				JsonObject entryJson = new JsonObject();
				JsonObject objectJson = new JsonObject();
				objectJson.add(entry.getValue().getClass().getName(),
						gson.toJsonTree(entry.getValue()));
				entryJson.add(entry.getKey().toString(), objectJson);
				mapWrapperElements.add(entryJson);
			}

		}

		mapWrapperJson.add(MapWrapper.class.getName(), mapWrapperElements);

		return mapWrapperJson;
	}

	private static MapWrapper jsonElementToMapWrapper(JsonElement mapWrapperJson)
			throws ClassNotFoundException {
		LOGGER.debug("jsonElementToMapWrapper");

		JsonArray entryListJson = (JsonArray) mapWrapperJson;
		MapWrapper mapWrapper = new MapWrapper();

		for (JsonElement entryJson : entryListJson) {
			MapEntry mapEntry = new MapEntry();
			Map.Entry entry = (Map.Entry) entryJson.getAsJsonObject()
					.entrySet().iterator().next();

			LOGGER.debug("map entry key read [{}]", entry.getKey());

			mapEntry.setKey(entry.getKey());

			Map.Entry innerEntry = (Map.Entry) ((JsonElement) entry.getValue())
					.getAsJsonObject().entrySet().iterator().next();
			Class innerEntryClass = Class.forName((String) innerEntry.getKey());

			LOGGER.debug("map entry value class [{}]", innerEntryClass);

			if (innerEntryClass.equals(MapWrapper.class)) {
				LOGGER.debug("map entry value is MapWrapper");
				mapEntry.setValue(jsonElementToMapWrapper((JsonElement) innerEntry
						.getValue()));
			} else if (innerEntryClass.equals(ListWrapper.class)) {
				LOGGER.debug("map entry value is ListWrapper");
				mapEntry.setValue(jsonElementToListWrapper((JsonElement) innerEntry
						.getValue()));
			} else {
				LOGGER.debug("map entry value is a regular object");
				mapEntry.setValue(jsonElementToObject(
						(JsonElement) innerEntry.getValue(), innerEntryClass));
			}
			LOGGER.debug("Map entry created [{}]", mapEntry);
			mapWrapper.getEntryList().add(mapEntry);
		}

		return mapWrapper;
	}

	private static ListWrapper jsonElementToListWrapper(
			JsonElement listWrapperJson) throws ClassNotFoundException {
		LOGGER.debug("jsonElementToListWrapper");

		JsonArray entryListJson = (JsonArray) listWrapperJson;
		ListWrapper listWrapper = new ListWrapper();

		for (JsonElement entryJson : entryListJson) {
			ListEntry listEntry = new ListEntry();
			Map.Entry entry = (Map.Entry) entryJson.getAsJsonObject()
					.entrySet().iterator().next();

			Class innerEntryClass = Class.forName((String) entry.getKey());

			LOGGER.debug("List entry value class [{}]", innerEntryClass);

			if (innerEntryClass.equals(MapWrapper.class)) {
				LOGGER.debug("map entry value is MapWrapper");
				listEntry.setValue(jsonElementToMapWrapper((JsonElement) entry
						.getValue()));
			} else if (innerEntryClass.equals(ListWrapper.class)) {
				LOGGER.debug("map entry value is ListWrapper");
				listEntry.setValue(jsonElementToListWrapper((JsonElement) entry
						.getValue()));
			} else {
				LOGGER.debug("map entry value is a regular object");
				listEntry.setValue(jsonElementToObject(
						(JsonElement) entry.getValue(), innerEntryClass));
			}
			listWrapper.getEntryList().add(listEntry);
			LOGGER.debug("List entry created [{}]", listEntry);
		}

		return listWrapper;
	}

	private static Object jsonElementToObject(JsonElement objectJson,
			Class clazz) {
		Gson gson = getGson();
		return gson.fromJson(objectJson, clazz);
	}

	private static JsonObject listWrapperToJson(ListWrapper listWrapper) {
		LOGGER.debug("listWrapperToJson [{}]", listWrapper);

		JsonObject listWrapperJson = new JsonObject();

		JsonArray listWrapperElements = new JsonArray();
		for (ListEntry entry : listWrapper.getEntryList()) {
			LOGGER.debug("ListEntry [{}]", entry);

			if (entry.getValue().getClass().equals(MapWrapper.class)) {
				LOGGER.debug("ListEntry value is a MapWrapper");
				listWrapperElements.add(mapWrapperToJson((MapWrapper) entry
						.getValue()));
			} else if (entry.getValue().getClass().equals(ListWrapper.class)) {
				LOGGER.debug("ListEntry value is a ListWrapper");
				listWrapperElements.add(listWrapperToJson((ListWrapper) entry
						.getValue()));
			} else {
				LOGGER.debug("ListEntry value is a regular object");

				Gson gson = getGson();
				JsonObject objectJson = new JsonObject();
				objectJson.add(entry.getValue().getClass().getName(),
						gson.toJsonTree(entry.getValue()));
				listWrapperElements.add(objectJson);
			}

		}

		listWrapperJson.add(ListWrapper.class.getName(), listWrapperElements);

		return listWrapperJson;
	}

	public static Object fromJson(String json, Class<?> clazz)
			throws ClassNotFoundException {
		Gson gson = getGson();
		return gson.fromJson(json, clazz);
	}

	public static Object toObject(String json, String classPackage)
			throws ClassNotFoundException {
		Gson gson = getGson();

		JsonParser parser = new JsonParser();

		JsonObject jsonObject = (JsonObject) parser.parse(json);

		Set entrySet = jsonObject.entrySet();

		Map.Entry firstElement = (Map.Entry) entrySet.iterator().next();

		Class clazz = Class.forName(classPackage
				+ ((String) firstElement.getKey()));

		return gson.fromJson((JsonElement) firstElement.getValue(), clazz);
	}

	public static Object toObject(String json, Class clazz)
			throws ClassNotFoundException {
		LOGGER.debug("toObject json [{}] class [{}]", json, clazz.getName());
		Gson gson = getGson();

		JsonParser parser = new JsonParser();

		JsonObject jsonObject = (JsonObject) parser.parse(json);

		Set entrySet = jsonObject.entrySet();

		Map.Entry firstElement = (Map.Entry) entrySet.iterator().next();

		if (clazz.equals(MapWrapper.class)) {
			LOGGER.debug("Class specified is MapWrapper, skipping regular marshalling");

			return jsonElementToMapWrapper((JsonElement) firstElement
					.getValue());
		}
		if (clazz.equals(ListWrapper.class)) {
			LOGGER.debug("Class specified is ListWrapper, skipping regular marshalling");
			return jsonElementToListWrapper((JsonElement) firstElement
					.getValue());
		}

		LOGGER.debug("Regular marshalling [{}], [{}]", firstElement.getKey(),
				firstElement.getValue());

		return gson.fromJson((JsonElement) firstElement.getValue(), clazz);
	}

	public static Object toObject(String json, Class clazz, String paramName)
			throws ClassNotFoundException {
		Gson gson = getGson();

		JsonParser parser = new JsonParser();

		JsonObject jsonObject = (JsonObject) parser.parse(json);

		Set entrySet = jsonObject.entrySet();

		Map.Entry firstElement = (Map.Entry) entrySet.iterator().next();

		jsonObject = (JsonObject) firstElement.getValue();

		entrySet = jsonObject.entrySet();

		Iterator iterator = entrySet.iterator();

		JsonElement jsonElem = null;

		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();

			if (((String) entry.getKey()).equalsIgnoreCase(paramName)) {
				jsonElem = (JsonElement) entry.getValue();
				break;
			}
		}

		if (jsonElem != null) {
			return gson.fromJson(jsonElem, clazz);
		}
		return null;
	}

	public static Gson getGson(Object object) {
		Field[] fields = object.getClass().getDeclaredFields();

		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(XMLGregorianCalendar.class,
				new XMLGregorianCalendarTypeAdapter());

		Gson gson = null;
		for (Field field : fields) {
			Annotation[] annotations = field.getDeclaredAnnotations();

			for (Annotation annotation : annotations) {
				if (annotation instanceof Expose) {
					gson = gsonBuilder.excludeFieldsWithoutExposeAnnotation()
							.setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
					return gson;
				}
			}
		}

		gson = gsonBuilder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
		return gson;
	}

	public static Gson getGson() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(XMLGregorianCalendar.class,
				new XMLGregorianCalendarTypeAdapter());
		return gsonBuilder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
	}

	static {
		classesNotToMarshal.put(String.class.getName(), String.class);
		classesNotToMarshal.put(Integer.class.getName(), Integer.class);
		classesNotToMarshal.put(Long.class.getName(), Long.class);
		classesNotToMarshal.put(Date.class.getName(), Date.class);
		classesNotToMarshal.put(BigInteger.class.getName(), BigInteger.class);
		classesNotToMarshal.put(BigDecimal.class.getName(), BigDecimal.class);
		classesNotToMarshal.put(XMLGregorianCalendar.class.getName(),
				XMLGregorianCalendar.class);
	}

	public static class XMLGregorianCalendarTypeAdapter implements
			JsonDeserializer<XMLGregorianCalendar>,
			JsonSerializer<XMLGregorianCalendar> {
		public XMLGregorianCalendar deserialize(JsonElement json, Type typeOfT,
				JsonDeserializationContext context) throws JsonParseException {
			String sDate = json.getAsJsonPrimitive().getAsString();
			try {
				DatatypeFactory dtf = DatatypeFactory.newInstance();
				return dtf.newXMLGregorianCalendar(sDate);
			} catch (DatatypeConfigurationException ex) {
				JsonUtility.LOGGER.error("", ex);
			}
			return null;
		}

		public JsonElement serialize(XMLGregorianCalendar source, Type type,
				JsonSerializationContext context) {
			return new JsonPrimitive(source.toXMLFormat());
		}
	}

	public static class BigDecimalCurrencyTypeAdapter implements
			JsonSerializer<BigDecimal> {
		public JsonElement serialize(BigDecimal source, Type type,
				JsonSerializationContext context) {
			DecimalFormat decimalFormat = new DecimalFormat("#0.00;-#0.00");
			return new JsonPrimitive(decimalFormat.format(source.doubleValue()));
		}
	}
}