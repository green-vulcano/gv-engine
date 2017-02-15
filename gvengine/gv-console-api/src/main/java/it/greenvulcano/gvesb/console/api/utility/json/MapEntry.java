package it.greenvulcano.gvesb.console.api.utility.json;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement(name = "mapEntry")
@XmlJavaTypeAdapter(MapEntryAdapter.class)
public class MapEntry implements Serializable {
	private static final long serialVersionUID = -6585689580675662955L;
	private static HashMap<String, Class> keyMap = new HashMap();
	private Object key;
	private Object value;
	private String clazz;

	public MapEntry() {
	}

	public MapEntry(Object key, Object value) {
		if (keyMap.get(key.getClass().getName()) == null) {
			throw new IllegalArgumentException("Class ["
					+ key.getClass().getName() + "] is not allowed to be a key");
		}

		this.key = key;

		if (value instanceof Map) {
			this.value = new MapWrapper((Map) value);
			this.clazz = MapWrapper.class.getName();
		} else if (value instanceof List) {
			this.value = new ListWrapper((List) value);
			this.clazz = ListWrapper.class.getName();
		} else {
			this.value = value;
			this.clazz = value.getClass().toString();
		}
	}

	public Object getKey() {
		return this.key;
	}

	public void setKey(Object key) {
		this.key = key;
	}

	public Object getValue() {
		return this.value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public String getClazz() {
		return this.clazz;
	}

	public void setClazz(String clazz) {
		this.clazz = clazz;
	}

	public String toString() {
		return "MapEntry [key=" + this.key + ", value=" + this.value
				+ ", clazz=" + this.clazz + "]";
	}

	static {
		keyMap.put(String.class.getName(), String.class);
		keyMap.put(Integer.class.getName(), Integer.class);
		keyMap.put(Long.class.getName(), Long.class);
		keyMap.put(BigInteger.class.getName(), BigInteger.class);
		keyMap.put(BigDecimal.class.getName(), BigDecimal.class);
	}
}
