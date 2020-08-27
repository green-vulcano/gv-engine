package it.greenvulcano.gvesb.iam.domain.mongodb;

import org.bson.Document;
import org.json.JSONObject;

public class PropertyBson extends it.greenvulcano.gvesb.iam.domain.Property {
    
    public static final String COLLECTION_NAME = "properties";
    
    private final String key, value;    
    private final Long settingTimestamp;    
    private final String username, originAddress;
    
    public PropertyBson(Document property) {    
        
        this.key = property.getString("key");
        this.value = property.getString("value");        
        this.settingTimestamp = property.getLong("setting_timestamp");
        this.username = property.getString("username");
        this.originAddress = property.getString("origin_address");
    }
    
    public PropertyBson(String key, String value, String username, String originAddress) {
        super();
        this.key = key;
        this.value = value;
        this.settingTimestamp = System.currentTimeMillis();
        this.username = username;
        this.originAddress = originAddress;
    }

    @Override
    public String getKey() {    
        return key;
    }
    
    @Override
    public String getValue() {    
        return value;
    }
    
    @Override
    public Long getSettingTimestamp() {    
        return settingTimestamp;
    }
    
    @Override
    public String getUsername() {    
        return username;
    }
    
    @Override
    public String getOriginAddress() {    
        return originAddress;
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + ((originAddress == null) ? 0 : originAddress.hashCode());
        result = prime * result + ((settingTimestamp == null) ? 0 : settingTimestamp.hashCode());
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        result = prime * result + ((username == null) ? 0 : username.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PropertyBson other = (PropertyBson) obj;
        if (originAddress == null) {
            if (other.originAddress != null)
                return false;
        } else if (!originAddress.equals(other.originAddress))
            return false;
        if (settingTimestamp == null) {
            if (other.settingTimestamp != null)
                return false;
        } else if (!settingTimestamp.equals(other.settingTimestamp))
            return false;
        if (key == null) {
            if (other.key != null)
                return false;
        } else if (!key.equals(other.key))
            return false;
        if (username == null) {
            if (other.username != null)
                return false;
        } else if (!username.equals(other.username))
            return false;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }
    
    @Override
    public String toString() {
           return toJSONObject().toString();
    }
    
    public JSONObject toJSONObject() {
        JSONObject property = new JSONObject();
        
        property.put("key", key)
            .put("value", value)
            .put("setting_timestamp", settingTimestamp)
            .put("username", username)
            .put("origin_address", originAddress);
        
           return property;
    }    
    
}