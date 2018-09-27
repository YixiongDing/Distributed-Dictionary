all: DictionaryServer.java DictionaryClient.java Message.java
	javac DictionaryServer.java
	jar cfe DictionaryServer.jar DictionaryServer DictionaryServer*.class Message.class
	javac DictionaryClient.java
	jar cfe DictionaryClient.jar DictionaryClient DictionaryClient*.class Message.class

clean:
	rm -rf *.class

.PHONY: clean