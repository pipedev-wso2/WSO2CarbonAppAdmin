package com.wso2.util;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.awaitility.Awaitility;
import org.wso2.carbon.application.mgt.stub.ApplicationAdminExceptionException;
import org.wso2.carbon.application.mgt.stub.types.carbon.ApplicationMetadata;
import org.wso2.carbon.application.mgt.stub.types.carbon.ArtifactDeploymentStatus;
import org.wso2.carbon.application.mgt.stub.upload.types.carbon.UploadedFileItem;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.authenticator.stub.LogoutAuthenticationExceptionException;
import org.wso2.carbon.service.mgt.stub.types.carbon.ServiceMetaData;
import org.wso2.carbon.service.mgt.stub.types.carbon.ServiceMetaDataWrapper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.activation.DataHandler;

public class WSO2CarbonAppAdmin {
	private static Logger logger = LogManager.getLogger();

	public static final String DEPLOY = "deploy";
	public static final Option deploy = Option.builder("d").argName("deploy").required(true).longOpt("deploy")
			.desc("Deploy CAR").build();

	public static final String UNDEPLOY = "undeploy";
	public static final Option undeploy = Option.builder("u").argName("undeploy").required(true).longOpt("undeploy")
			.desc("Undeploy CAR").build();

	public static final String FILE = "file";
	public static final Option file = Option.builder("f").argName("file").required(false).hasArg().longOpt("file")
			.desc("CAR file").build();

	public static final String CARAPP_NAME = "carapp_name";
	public static final Option carapp_name = Option.builder("n").argName("carapp_name").required(true).hasArg()
			.longOpt("carapp_name").desc("CAR APP name").build();

	public static void main(String[] args) throws LoginAuthenticationExceptionException,
			LogoutAuthenticationExceptionException, ApplicationAdminExceptionException, IOException, ParseException {

		Options options = new Options();

		OptionGroup optgrp = new OptionGroup();
		optgrp.addOption(deploy);
		optgrp.addOption(undeploy);

		options.addOptionGroup(optgrp);
		options.addOption(file);
		options.addOption(carapp_name);

		try {
			CommandLineParser parser = new DefaultParser();
			CommandLine cmdline = parser.parse(options, args);

			if (cmdline.hasOption(DEPLOY)) {
				if ((!cmdline.hasOption(FILE)) || (!cmdline.hasOption(CARAPP_NAME))) {
					throw new ParseException("Missing required arg: FILE, CARAPP_NAME");
				} else {
					deploy(cmdline.getOptionValue(FILE), cmdline.getOptionValue(CARAPP_NAME));
				}
			}

			if (cmdline.hasOption(UNDEPLOY)) {
				if ((!cmdline.hasOption(CARAPP_NAME))) {
					throw new ParseException("Missing required arg: CARAPP_NAME");
				} else {
					undeploy(cmdline.getOptionValue(CARAPP_NAME));
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(-1);
		}

		System.exit(0);
	}

	private static void deploy(String file, String cappName) throws Exception {
		
		logger.debug("---> Deploy option: file=" + file + ", cappName=" + cappName);

		try {
			//Properties
			Properties prop = new Properties();
			InputStream propIS = ClassLoader.getSystemResourceAsStream("WSO2CarbonAppAdmin.properties");
			prop.load(propIS);

			System.setProperty("javax.net.ssl.trustStore", prop.getProperty("javax.net.ssl.trustStore"));
			System.setProperty("javax.net.ssl.trustStorePassword",
					prop.getProperty("javax.net.ssl.trustStorePassword"));
			System.setProperty("javax.net.ssl.trustStoreType", prop.getProperty("javax.net.ssl.trustStoreType"));
			String backEndUrl = prop.getProperty("backEndUrl");

			//Login
			LoginAdminServiceClient login = new LoginAdminServiceClient(backEndUrl);
			String session = login.authenticate(prop.getProperty("wso2username"), prop.getProperty("wso2password"));

			//ApplicationAdmin WS
			ApplicationAdminClient applicationAdminClient = new ApplicationAdminClient(backEndUrl, session);

			//Deploy
			File f = new File(file);
			if (!f.exists()) {
				throw new FileNotFoundException();
			}
			
			UploadedFileItem item = new UploadedFileItem();
			URI uri = f.toURI();
			DataHandler param = new DataHandler(uri.toURL());
			item.setDataHandler(param);
			item.setFileName(f.getName());
			item.setFileType("jar");
			List<UploadedFileItem> items = new ArrayList<UploadedFileItem>();
			items.add(item);

			UploadedFileItem[] items2 = items.toArray(new UploadedFileItem[items.size()]);

			//CarbonAppUploader WS
			CarbonAppUploaderAdminClient cappUploaderAdminClient = new CarbonAppUploaderAdminClient(backEndUrl,
					session);
			cappUploaderAdminClient.uploadApp(items2);

			Integer t = 0;
			while((t < 10) &&(!applicationAdminClient.appExists(cappName))) {
				Thread.sleep(5000);
				t++;
			}
			
			if(t == 10) {
				throw new Exception("CAR " + cappName + " not deployed (appExists).");
			}

			//Check Application status
			logger.debug("Wait for deployment status (status) ....");
			ApplicationMetadata appMeta = applicationAdminClient.getAppData(cappName);
			t = 0;
			while((t < 10) &&(!applicationAdminClient.hasDeploymentStatus(appMeta, "Deployed"))) {
				Thread.sleep(5000);
				t++;
			}

			if(t == 10) {
				throw new Exception("CAR " + cappName + " not deployed.");
			}

			System.out.println("CAR " + cappName + " deployed correctly (hasDeploymentStatus).");
			
			listApplications();

			//LogOut
			login.logOut();

		} catch (FileNotFoundException e) {
			System.out.println("File " + file + " not found.");
			e.printStackTrace();
		} catch (RemoteException e) {
			System.out.println("Remote exception");
			e.printStackTrace();
		}
	}

	private static void undeploy(String cappName) throws Exception {
		logger.debug("---> Undeploy Option: cappName=" + cappName);

		try {
			//Properties
			Properties prop = new Properties();
			InputStream propIS = ClassLoader.getSystemResourceAsStream("WSO2CarbonAppAdmin.properties");
			prop.load(propIS);
	
			System.setProperty("javax.net.ssl.trustStore", prop.getProperty("javax.net.ssl.trustStore"));
			System.setProperty("javax.net.ssl.trustStorePassword", prop.getProperty("javax.net.ssl.trustStorePassword"));
			System.setProperty("javax.net.ssl.trustStoreType", prop.getProperty("javax.net.ssl.trustStoreType"));
			String backEndUrl = prop.getProperty("backEndUrl");
	
			//Login
			LoginAdminServiceClient login = new LoginAdminServiceClient(backEndUrl);
			String session = login.authenticate(prop.getProperty("wso2username"), prop.getProperty("wso2password"));
	
			//ApplicationAdmin WS
			ApplicationAdminClient applicationAdminClient = new ApplicationAdminClient(backEndUrl, session);
	
			//Undeploy
			applicationAdminClient.deleteApplication(cappName);
	
			System.out.println("CAR" + cappName + " successfully undeployed.");
			
			listApplications();
	
			//LogOut
			login.logOut();
		} catch (RemoteException e) {
			System.out.println("Remote exception");
			e.printStackTrace();
		}
	}
	
	private static void listApplications() throws Exception {
		logger.debug("---> List Applications");

		try {
			//Properties
			Properties prop = new Properties();
			InputStream propIS = ClassLoader.getSystemResourceAsStream("WSO2CarbonAppAdmin.properties");
			prop.load(propIS);
	
			System.setProperty("javax.net.ssl.trustStore", prop.getProperty("javax.net.ssl.trustStore"));
			System.setProperty("javax.net.ssl.trustStorePassword", prop.getProperty("javax.net.ssl.trustStorePassword"));
			System.setProperty("javax.net.ssl.trustStoreType", prop.getProperty("javax.net.ssl.trustStoreType"));
			String backEndUrl = prop.getProperty("backEndUrl");
	
			//Login
			LoginAdminServiceClient login = new LoginAdminServiceClient(backEndUrl);
			String session = login.authenticate(prop.getProperty("wso2username"), prop.getProperty("wso2password"));
	
			//ApplicationAdmin WS
			ApplicationAdminClient applicationAdminClient = new ApplicationAdminClient(backEndUrl, session);
	
			//List Applications
			String[] applicationList = applicationAdminClient.listApplications();
			System.out.println("List Applications:");
			if (applicationList != null) {
				for (String serviceData : applicationList) {
					System.out.println(serviceData);
					ApplicationMetadata appMetadata = applicationAdminClient.getAppData(serviceData);
					for (ArtifactDeploymentStatus status : appMetadata.getArtifactsDeploymentStatus()) {
						System.out.println("--->>>" + status.getArtifactName() + ", " + status.getDeploymentStatus());
					}
				}
			}
	
			//LogOut
			login.logOut();
		} catch (RemoteException e) {
			System.out.println("Remote exception");
			e.printStackTrace();
		}
	}
	
}