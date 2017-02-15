package it.greenvulcano.gvesb.console.api.model;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;


@Entity("trace_level_service")
public class TraceLevelService {

	@Id private ObjectId _id;

	//@Field("serviceName")
	private String serviceName;
	
	//@Field("enabled")
	private int enabled;
	
	//@Field("traceLevel")
	private String traceLevel; // INFO/DEBUG

	
	public String getIdToString() {
		return _id.toString();
	}

	public void setIdString(String _id) {
		this._id = new ObjectId(_id);
	}
	
	public ObjectId getId() {
		return _id;
	}
	
	public void setId(ObjectId _id) {
		this._id = _id;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public int getEnabled() {
		return enabled;
	}

	public void setEnabled(int enabled) {
		this.enabled = enabled;
	}

	public String getTraceLevel() {
		return traceLevel;
	}

	public void setTraceLevel(String traceLevel) {
		this.traceLevel = traceLevel;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TraceLevelService [_id=");
		builder.append(_id);
		builder.append(", serviceName=");
		builder.append(serviceName);
		builder.append(", enabled=");
		builder.append(enabled);
		builder.append(", traceLevel=");
		builder.append(traceLevel);
		builder.append("]");
		return builder.toString();
	}

}
