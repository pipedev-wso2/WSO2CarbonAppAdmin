package com.wso2.util;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.wso2.carbon.application.mgt.stub.ApplicationAdminExceptionException;
import org.wso2.carbon.application.mgt.stub.ApplicationAdminStub;
import org.wso2.carbon.service.mgt.stub.ServiceAdminStub;
import org.wso2.carbon.service.mgt.stub.types.carbon.ServiceMetaDataWrapper;
import org.wso2.carbon.application.mgt.stub.types.carbon.ApplicationMetadata;
import org.wso2.carbon.application.mgt.stub.upload.CarbonAppUploaderStub;
import org.wso2.carbon.application.mgt.stub.upload.types.carbon.UploadedFileItem;

import java.rmi.RemoteException;

public class CarbonAppUploaderAdminClient {
	private final String serviceName = "CarbonAppUploader";
	private CarbonAppUploaderStub cappUploaderAdminStub;
	private String endPoint;

	public CarbonAppUploaderAdminClient(String backEndUrl, String sessionCookie) throws AxisFault {
		this.endPoint = backEndUrl + "/services/" + serviceName;
		cappUploaderAdminStub = new CarbonAppUploaderStub(endPoint);
		// Authenticate Your stub from sessionCooke
		ServiceClient serviceClient;
		Options option;

		serviceClient = cappUploaderAdminStub._getServiceClient();
		option = serviceClient.getOptions();
		option.setManageSession(true);
		option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, sessionCookie);
	}

	public void uploadApp(UploadedFileItem[] items) throws RemoteException {
		cappUploaderAdminStub.uploadApp(items);
	}

}