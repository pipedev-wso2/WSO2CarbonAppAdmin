package com.wso2.util;

import org.apache.axis2.AxisFault;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.authenticator.stub.LogoutAuthenticationExceptionException;
import org.apache.axis2.context.ServiceContext;
import java.rmi.RemoteException;

public class LoginAdminServiceClient {
	private static Logger logger = LogManager.getLogger();
	
	private final String serviceName = "AuthenticationAdmin";
	private AuthenticationAdminStub authenticationAdminStub;
	private String endPoint;

	public LoginAdminServiceClient(String backEndUrl) throws AxisFault {
		this.endPoint = backEndUrl + "/services/" + serviceName;
		authenticationAdminStub = new AuthenticationAdminStub(endPoint);
	}

	public String authenticate(String userName, String password)
			throws RemoteException, LoginAuthenticationExceptionException {

		String sessionCookie = null;

		if (authenticationAdminStub.login(userName, password, "localhost")) {
			logger.debug("---> Login Successful");

			ServiceContext serviceContext = authenticationAdminStub._getServiceClient().getLastOperationContext()
					.getServiceContext();
			sessionCookie = (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);
			logger.debug("---> SESSION COOKIE=" + sessionCookie);
		}

		return sessionCookie;
	}

	public void logOut() throws RemoteException, LogoutAuthenticationExceptionException {
		authenticationAdminStub.logout();
	}
}