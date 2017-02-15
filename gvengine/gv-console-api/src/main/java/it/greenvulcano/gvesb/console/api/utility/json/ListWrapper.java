package it.greenvulcano.gvesb.console.api.utility.json;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "listWrapper")
public class ListWrapper implements Serializable {
	private static final long serialVersionUID = -7495130530616362556L;
	private List<ListEntry> entryList;

	@XmlAnyElement
	public List<ListEntry> getEntryList() {
		return this.entryList;
	}

	public void setEntryList(List<ListEntry> entryList) {
		this.entryList = entryList;
	}

	public ListWrapper() {
		this.entryList = new ArrayList();
	}

	public ListWrapper(List<?> inputList) {
		this.entryList = new ArrayList();

		for (Iterator i$ = inputList.iterator(); i$.hasNext();) {
			Object entry = i$.next();
			this.entryList.add(new ListEntry(entry));
		}
	}

	public String toString() {
		return "ListWrapper [entryList=" + this.entryList + "]";
	}

	public List<Object> getList() {
		List list = new ArrayList();

		for (ListEntry listEntry : this.entryList) {
			if (listEntry.getValue() instanceof MapWrapper) {
				list.add(((MapWrapper) listEntry.getValue()).getMap());
			} else if (listEntry.getValue() instanceof ListWrapper) {
				list.add(((ListWrapper) listEntry.getValue()).getList());
			} else {
				list.add(listEntry.getValue());
			}

		}

		return list;
	}
}
