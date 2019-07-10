
# NLS-Service

This service computes the semantic similarity between based on a specified word2vec model.

# Compilation

This project requires Java 8 and is not compatible with newer Java versions right now.

to compile the project simply change into the projects root directory and run
```
mvn install
```

This will compile a runnable jar at `$projectDir/language-analysis-app/target/language-analysis-app-1.0-SNAPSHOT-standalone.jar`

# Start the service
The service takes a very long time to start and about 4GB of RAM to load the word2vec model.
Therefore it is recommented to start the service only once and let it run in the background, meanwhile any (client)application code is invoked independently from this service.

The NLS-Service is started by invoking the specified jar from the step above
```
java -jar -Xmx4G language-analysis-app-1.0-SNAPSHOT-standalone.jar
```

# Customize Ip and location of the word2vec model

The service is accessible via *port* `1234`, this port is hardcoded right now.

The configuration file `application.conf` at `$projectDir/language-analysis-app/src/main/resources` is parsed to determine the ip and the location of the word2vec model.

To change the ip you have to adapt the properties `languageanalysis-app.interface`. 
Right now this value is set to ip `172.16.1.254` to allow for easy docker subnet specification.  
You may want to change this value to `127.0.0.1` to deploy and access the service locally only.
If you need the service externally (on a different pc or docker container) you should enter the ip address of your ethernet/wifi interface.

To change the used word2vec model modify the vaulue of `languageanalysis-app.language.word2vec.googleVector` to your location. Per default the service assumes the google new vector to be located at `/home/TestData/GoogleNews-vectors-negative300.bin.gz`
