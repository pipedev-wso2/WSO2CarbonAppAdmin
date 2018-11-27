# WSO2CarbonAppAdmin

Deploy/Delete Carbon Application (CAR)

# compile

mvn package

# deploy

java -jar target/WSO2CarbonAppAdmin.jar -deploy -f CAR_FILE_PATH -n CAR_FILE_NAME

-d -> deploy <br />
-f -> CAR file <br />
-n -> CAR Application name <br />

Example:

java -jar target/WSO2CarbonAppAdmin.jar -deploy -f /tmp/SampleCompositeApplication_1.0.0.car -n SampleCompositeApplication_1.0.0

# delete

java -jar target/WSO2CarbonAppAdmin.jar -delete -n CAR_FILE_NAME

-u -> undeploy (delete) <br />
-n -> CAR Application name <br />

Example:

java -jar target/WSO2CarbonAppAdmin.jar -delete -n SampleCompositeApplication_1.0.0


