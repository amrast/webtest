
# NLS-Service

This service computes the semantic similarity between based on a specified word2vec model.
A ready to use docker image can be found [here](https://hub.docker.com/r/jh00/nlsservice).

# Docker

The NLS-Service requires a static ip configuration. In the [provided docker image](https://hub.docker.com/r/jh00/nlsservice) this ip is `172.16.1.254`.

To run the service as docker container you have to first create a user defined docker network, e.g.
```
docker network create --subnet 172.16.1.0/24 nls-network
```

Make sure you have configured docker to use more than 4GB RAM, otherwise the service start process may return killed after a few minutes.  
Then you can invoke the NLS-Service container with a static ip address via
```
docker run -it --rm -p 1234:1234 --ip 172.16.1.254 --net=nls-network jh00/nlsservice
```
Note: Starting the service takes very much time (5-10 minutes and longer depending on the computers performance).


The port expose (`-p`) allows the host environment to access the service.  
Moreover, this allows you to access the service via `host.docker.internal` from any container (executed with bridge or host network).

You can test that the service works correctly with a simple curl command e.g.  
from within another container
```
curl -X GET 'host.docker.internal:1234/word2vec/wordsimilarity?word1=test&word2
```
from within another container in the same network (nls-network)
```
curl -X GET 'http://172.16.1.254:1234/word2vec/wordsimilarity?word1=test&word2
```
from the host pc (TODO verify if this works)
```
curl -X GET 'http://172.16.1.254:1234/word2vec/wordsimilarity?word1=test&word2
```

should return 1.0

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
