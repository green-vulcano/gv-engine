package it.greenvulcano.gvesb.console.api.utility;



public final class Constants {

	private Constants(){
		super(); 
	}
	
	 public static final String ISO_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
	
    // ******************* EXCEPTION ******************
    public static final String EXCEPTION_MSG_CODE = "code [";
    public static final String EXCEPTION_MSG_DESC = "] description [";
	
	// ******************* HEADER KEY ******************
	public static final String HEADER_RETRIEVE_CAMERA_DESTINATION_REQUEST = "RetrieveCameraDestinationRequest";
	public static final String HEADER_RETRIEVE_CAMERA_DESTINATION_REQUEST_JSON = "RetrieveCameraDestinationRequestJson";
	public static final String HEADER_GET_RTP_DESTINATION_RESPONSE = "GetRtpDestinationServerResponse";
	
	
	public static final String HEADER_INSERT_DEVICES_REQUEST = "InsertDevicesRequest";
	public static final String HEADER_INSERT_DEVICES_REQUEST_JSON = "InsertDevicesRequestJson";
	public static final String HEADER_GET_AUTH_TOKEN_RESPONSE = "GetAuthTokenResponse";
	public static final String HEADER_CREATE_RTSP_RESPONSE = "CreateRtspResponse";
	public static final String HEADER_LIST_VIDEOS_RESPONSE = "ListVideosResponse";
	public static final String HEADER_START_BROADCAST_RESPONSE = "StartBroadcastResponse";
	public static final String HEADER_UPDATE_DEVICES_RESPONSE = "UpdateDevicesResponse";
	public static final String HEADER_RETRIEVE_PORTNUMBER_RESPONSE = "RetrievePortNumberResponse";

	public static final String HEADER_AUTH_TOKEN = "authToken";
	public static final String HEADER_VIDEO_REF = "videoRef";
	public static final String HEADER_RTSP_REF = "rtspRef";
	public static final String HEADER_RTSP_NAME = "rtspName";

	
	// ******************* DI ******************
	public static final Integer DI_SUCCESS = 0;
	
	public static final int		HTTP_500										= 500;
	public static final int		HTTP_200										= 200;
	public static final int		HTTP_404										= 404;
	public static final String	SERVICE_NOT_AVAILABLE							= "Service not available";
	public static final String	SUCCESS											= "Success";
	public static final String	GENERIC_ERROR									= "Generic error. For more details check the logs";
		
	// ******************* REST CLIENT ******************
	public static final String	HTTP_METHOD_POST								= "POST";
	public static final String	CONTENT_TYPE_APPLICATION_JSON					= "application/json";

}
