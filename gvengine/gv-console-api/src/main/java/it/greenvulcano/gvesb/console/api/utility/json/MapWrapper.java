package it.greenvulcano.gvesb.console.api.utility.json;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "mapWrapper")
public class MapWrapper implements Serializable {
	private static final long serialVersionUID = -6702783028385422322L;
	private List<MapEntry> entryList;

	public MapWrapper() {
		this.entryList = new ArrayList();
	}

	public MapWrapper(Map<?, ?> inputMap) {
		this.entryList = new ArrayList();

		for (Map.Entry entrySet : inputMap.entrySet())
			if (entrySet.getValue() != null)
				this.entryList.add(new MapEntry(entrySet.getKey(), entrySet
						.getValue()));
	}

	@XmlAnyElement
	public List<MapEntry> getEntryList() {
		return this.entryList;
	}

	public void setEntryList(List<MapEntry> entryList) {
		this.entryList = entryList;
	}

	public String toString() {
		return "MapWrapper [entryList=" + this.entryList + "]";
	}

	public Map<Object, Object> getMap() {
		Map map = new HashMap();

		for (MapEntry mapEntry : this.entryList) {
			if (mapEntry.getValue() != null) {
				if (mapEntry.getValue() instanceof MapWrapper)
					map.put(mapEntry.getKey(),
							((MapWrapper) mapEntry.getValue()).getMap());
				else if (mapEntry.getValue() instanceof ListWrapper)
					map.put(mapEntry.getKey(),
							((ListWrapper) mapEntry.getValue()).getList());
				else {
					map.put(mapEntry.getKey(), mapEntry.getValue());
				}
			}

		}

		return map;
	}
}
