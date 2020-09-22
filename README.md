# prometheus-sandbox

## Usage

`docker run -d -p 9091:9091 prom/pushgateway`

`mvn clean install`

`java -cp target/my-app-1.0-SNAPSHOT-jar-with-dependencies.jar com.mycompany.app.App`

You can then go to http://localhost:9091 to view your metrics that were pushed
