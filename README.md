# WSO2CarbonAppAdmin

Deploy/Delete Carbon Application (CAR)

# compile

mvn package

# deploy

java -jar target/WSO2CarbonAppAdmin.jar -d -f CAR_FILE_PATH -n CAR_FILE_NAME

-d -> deploy <br />
-f -> CAR file <br />
-n -> CAR Application name <br />

Example:

java -jar target/WSO2CarbonAppAdmin.jar -d -f /tmp/SampleCompositeApplication_1.0.0.car -n SampleCompositeApplication_1.0.0

# delete

java -jar target/WSO2CarbonAppAdmin.jar -u -n CAR_FILE_NAME

-u -> undeploy (delete) <br />
-n -> CAR Application name <br />

Example:

java -jar target/WSO2CarbonAppAdmin.jar -u -n SampleCompositeApplication_1.0.0


