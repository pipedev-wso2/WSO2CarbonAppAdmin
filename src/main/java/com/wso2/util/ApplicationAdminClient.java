package com.wso2.util;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.omg.CORBA.portable.ApplicationException;
import org.wso2.carbon.application.mgt.stub.ApplicationAdminExceptionException;
import org.wso2.carbon.application.mgt.stub.ApplicationAdminStub;
import org.wso2.carbon.service.mgt.stub.ServiceAdminStub;
import org.wso2.carbon.service.mgt.stub.types.carbon.ServiceMetaDataWrapper;
import org.wso2.carbon.application.mgt.stub.types.carbon.ApplicationMetadata;
import org.wso2.carbon.application.mgt.stub.types.carbon.ArtifactDeploymentStatus;
import org.wso2.carbon.application.mgt.stub.upload.types.carbon.UploadedFileItem;

import java.rmi.RemoteException;
import java.util.concurrent.atomic.AtomicBoolean;

public class ApplicationAdminClient {
	private static Logger logger = LogManager.getLogger();

	private final String serviceName = "ApplicationAdmin";
	private ApplicationAdminStub applicationAdminStub;
	private String endPoint;

	public ApplicationAdminClient(String backEndUrl, String sessionCookie) throws AxisFault {
		this.endPoint = backEndUrl + "/services/" + serviceName;
		applicationAdminStub = new ApplicationAdminStub(endPoint);
		ServiceClient serviceClient;
		Options option;

		serviceClient = applicationAdminStub._getServiceClient();
		option = serviceClient.getOptions();
		option.setManageSession(true);
		option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, sessionCookie);
	}

	public String[] listApplications() throws RemoteException, ApplicationAdminExceptionException {
		return applicationAdminStub.listAllApplications();
	}

	public ApplicationMetadata getAppData(String app) throws RemoteException, ApplicationAdminExceptionException {
		return applicationAdminStub.getAppData(app);
	}

	public Boolean appExists(String app) {
		logger.debug("---> appExists: app=" + app);
		Boolean ret = false;
		try {
			ApplicationMetadata appMeta = applicationAdminStub.getAppData(app);
			if ((appMeta != null)) {
				logger.debug("---> appMeta: " + appMeta.getAppName());
				if (appMeta.getAppName().concat("_").concat(appMeta.getAppVersion()).contentEquals(app)) {
					ret=true;
				}
			}
		} catch (Exception e) {
			logger.debug("---> appExists Exception");
		}

		return ret;
	}

	public Boolean hasDeploymentStatus(ApplicationMetadata metadata, String requiredStatus) {
		Boolean ret = false;
		try {
			logger.debug("---> hasDeploymentStatus: requiredStatus=" + requiredStatus);
			for (ArtifactDeploymentStatus status : metadata.getArtifactsDeploymentStatus()) {
				logger.debug("---> hasDeploymentStatus: status=" + status.getDeploymentStatus());
				if (matches(status, requiredStatus))
					ret = true;
			}
		} catch (Exception e) {

		}
		return ret;
	}

	private boolean matches(ArtifactDeploymentStatus status, String requiredStatus) {
		return status.isDeploymentStatusSpecified() && status.getDeploymentStatus().equals(requiredStatus);
	}

	public void deleteApplication(String cappName) throws RemoteException, ApplicationAdminExceptionException {
		applicationAdminStub.deleteApplication(cappName);
	}
}