package it.greenvulcano.gvesb.console.api.utility.json;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement(name = "listEntry")
@XmlJavaTypeAdapter(ListEntryAdapter.class)
public class ListEntry implements Serializable {
	private static final long serialVersionUID = 5007648518129595433L;
	private Object value;
	private String clazz;

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

	public ListEntry() {
	}

	public ListEntry(Object value) {
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

	public String toString() {
		return "ListEntry [value=" + this.value + ", clazz=" + this.clazz + "]";
	}
}
