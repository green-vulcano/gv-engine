package it.greenvulcano.gvesb.aop;


public interface GVServiceInstanceFields {

	public static final String GVC_SERVICE_INSTANCE_ID 	= "serviceInstanceId";
	public static final String GVC_SERVICE_NAME			= "serviceName";
	public static final String GVC_OPERATION_NAME		= "operationName";
	public static final String GVC_SUBOPERATION_NAME 	= "suboperationName";
	public static final String GVC_SYSTEM 				= "system";
	public static final String GVC_SUBSYSTEM 			= "subsystem";
	public static final String GVC_THREAD_NAME 			= "threadName";
	public static final String GVC_START_DATE 			= "startDate";
	public static final String GVC_END_DATE 			= "endDate";
	public static final String GVC_INPUT_OBJECT_TYPE 	= "inputObjectType";
	public static final String GVC_GV_BUFFER_OBJECT		= "inputObject";
	public static final String GVC_GV_BUFFER_PROPS		= "properties";
	public static final String GVC_INPUT_OBJECT_TYPE_OUT 	= "outputObjectType";
	public static final String GVC_GV_BUFFER_OBJECT_OUT		= "outputObject";
	public static final String GVC_GV_BUFFER_PROPS_OUT		= "outputProperties";
	
	public static final String GVC_INPUT_OBJECT_TYPE_XML 	= "application/xml";
	public static final String GVC_INPUT_OBJECT_TYPE_JSON 	= "application/json";
	public static final String GVC_INPUT_OBJECT_TYPE_BYTE 	= "application/octet-stream";
	
}
