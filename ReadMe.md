
# NLS-Service

This service computes the semantic similarity between based on a specified word2vec model.
A ready to use docker image can be found [here](https://hub.docker.com/r/jh00/nlsservice).

As default, the NLS-Service is configured to listen to ip `0.0.0.0` at port `1234`.
This means it exposes port 1234 on *all IPv4 addresses on the local machine*, i.e. any computer in the network is able to access the service through the hosts ethernet ip.  
If you want to use the service exclusively on your local machine (and deploy it via docker) you should change this configuration to `127.0.0.1` as described [below](#Customize-Ip-and-location-of-the-word2vec-model).


# Docker

#### <font color=orange>Make sure you have configured docker to use more than 4GB RAM, otherwise the service will not start properly and the process may return 'killed' after a few minutes.</font>


To be able to access the service outside of its docker container you should either  
1. expose the port `1234` to the host computer  
	expose the service to **localhost only**:
	```
		docker run -it --rm -p 127.0.0.1:1234:1234 jh00/nlsservice
	```
	expose the service to **all IPv4 addresses on the local machine** (this allows e.g. other machines in the hosts network to access the service via the hosts ip address):
	```
	docker run -it --rm -p 1234:1234 jh00/nlsservice
	```
	__OR__  
2. create a docker subnetwork, then other containers within the same docker network can access the service via its assigned ip
	```
	docker network create --subnet 172.16.1.0/24 nls-network
	docker run -it --rm --ip 172.16.1.254 --net=nls-network jh00/nlsservice
	```

**Note:  Starting the service takes very much time**  (5-10 minutes and longer depending on the computers performance).


You can test that the service works correctly with a simple curl command which should return "1.0" (depending on which network option you chose)  
1. from within another container
	```
	curl -X GET 'host.docker.internal:1234/word2vec/wordsimilarity?word1=test&word2=test'
	```
	from the host mathine
	```
	curl -X GET 'localhost:1234/word2vec/wordsimilarity?word1=test&word2=test'
	```
	
2.	from within another container in the same network (nls-network)
	```
	curl -X GET 'http://172.16.1.254:1234/word2vec/wordsimilarity?word1=test&word2=test'
	```



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

The configuration file `$projectDir/language-analysis-app/src/main/resources/application.conf` is parsed to determine the ip and the location of the word2vec model.

To change the ip you have to adapt the properties `languageanalysis-app.interface`. 
Right now this value is set to ip `0.0.0.0` for ease docker deployment.  
You may want to change this value to `127.0.0.1` to deploy and access the service locally only.
If you need the service externally (on a different pc or docker container) you should enter the ip address of the network interface used to access the host machine (e.g. VPN).

To change the used word2vec model modify the vaulue of `languageanalysis-app.language.word2vec.googleVector` to your location. Per default the service assumes the google new vector to be located at `/home/TestData/GoogleNews-vectors-negative300.bin.gz`
