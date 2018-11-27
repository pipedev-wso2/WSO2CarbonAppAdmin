# WSO2CarbonAppAdmin

Deploy/Delete Carbon Application (CAR)

# compile

mvn package

# deploy

java -jar target/WSO2CarbonAppAdmin.jar -deploy -f CAR_FILE_PATH -n CAR_FILE_NAME

<<<<<<< HEAD
-d -> deploy <br />
-f -> CAR file <br />
-n -> CAR Application name <br />
=======
-deploy -> deploy
-f -> CAR file
-n -> CAR Application name
>>>>>>> 4cf7018b7a57bc64a6884b26c0c87b6f433c7e70

Example:

java -jar target/WSO2CarbonAppAdmin.jar -deploy -f /tmp/SampleCompositeApplication_1.0.0.car -n SampleCompositeApplication_1.0.0

# delete

java -jar target/WSO2CarbonAppAdmin.jar -delete -n CAR_FILE_NAME

<<<<<<< HEAD
-u -> undeploy (delete) <br />
-n -> CAR Application name <br />
=======
-delete -> delete
-n -> CAR Application name
>>>>>>> 4cf7018b7a57bc64a6884b26c0c87b6f433c7e70

Example:

java -jar target/WSO2CarbonAppAdmin.jar -delete -n SampleCompositeApplication_1.0.0


