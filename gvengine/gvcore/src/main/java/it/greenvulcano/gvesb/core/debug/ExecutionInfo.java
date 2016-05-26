package it.greenvulcano.gvesb.core.debug;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

public class ExecutionInfo implements Serializable
{
    /**
     * 
     */
    private static final long             serialVersionUID = 1L;
    private String                        service;
    private String                        operation;
    private String                        nodeId;
    private String                        subflow;
    private transient Map<String, Object> environment;

    public ExecutionInfo(String service, String operation, String nodeId, String subflow,
            Map<String, Object> environment)
    {
        super();
        this.service = service;
        this.operation = operation;
        this.nodeId = nodeId;
        this.subflow = subflow;
        this.environment = environment;
    }

    public ExecutionInfo(ExecutionInfo copy)
    {
        super();
        this.service = copy.service;
        this.operation = copy.operation;
        this.nodeId = copy.nodeId;
        this.subflow = copy.subflow;
        this.environment = copy.environment;
    }

    public String getNodeId()
    {
        return nodeId;
    }

    public void setNodeId(String nodeId)
    {
        this.nodeId = nodeId;
    }

    public String getSubflow()
    {
        return subflow;
    }

    public void setSubflow(String subflow)
    {
        this.subflow = subflow;
    }

    public String getService()
    {
        return service;
    }

    public String getOperation()
    {
        return operation;
    }

    public void setEnvironment(Map<String, Object> env)
    {
        this.environment = env;
    }

    public Map<String, Object> getEnvironment()
    {
        return environment != null ? Collections.unmodifiableMap(environment) : null;
    }

    public String getUniqueKey()
    {
        return new StringBuilder(service).append("@@").append(operation).append("@@").append(subflow).toString();
    }

    public String toString()
    {
        return new StringBuilder(service).append("@@").append(operation).append("@@").append(subflow).append("@@").append(
                nodeId).toString();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((nodeId == null) ? 0 : nodeId.hashCode());
        result = prime * result + ((operation == null) ? 0 : operation.hashCode());
        result = prime * result + ((service == null) ? 0 : service.hashCode());
        result = prime * result + ((subflow == null) ? 0 : subflow.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ExecutionInfo other = (ExecutionInfo) obj;
        if (nodeId == null) {
            if (other.nodeId != null)
                return false;
        }
        else if (!nodeId.equals(other.nodeId))
            return false;
        if (operation == null) {
            if (other.operation != null)
                return false;
        }
        else if (!operation.equals(other.operation))
            return false;
        if (service == null) {
            if (other.service != null)
                return false;
        }
        else if (!service.equals(other.service))
            return false;
        if (subflow == null) {
            if (other.subflow != null)
                return false;
        }
        else if (!subflow.equals(other.subflow))
            return false;
        return true;
    }

}
