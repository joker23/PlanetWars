all:
	javac *.java

clean:
	rm *.class

RandomBot:
	wmname LG3D
	java -jar tools/PlayGame.jar maps/map7.txt 1000 1000 log.txt "java -jar example_bots/RandomBot.jar" "java -jar example_bots/DualBot.jar" | java -jar tools/ShowGame.jar

BullyBot:
	wmname LG3D
	java -jar tools/PlayGame.jar maps/map7.txt 1000 1000 log.txt "java MyBot" "java -jar example_bots/BullyBot.jar" | java -jar tools/ShowGame.jar

DualBot:
	wmname LG3D
	java -jar tools/PlayGame.jar maps/map7.txt 1000 1000 log.txt "java MyBot" "java -jar example_bots/DualBot.jar" | java -jar tools/ShowGame.jar

ProspectorBot:
	wmname LG3D
	java -jar tools/PlayGame.jar maps/map7.txt 1000 1000 log.txt "java MyBot" "java -jar example_bots/ProspectorBot.jar" | java -jar tools/ShowGame.jar

RageBot:
	wmname LG3D
	java -jar tools/PlayGame.jar maps/map7.txt 1000 1000 log.txt "java MyBot" "java -jar example_bots/RageBot.jar" | java -jar tools/ShowGame.jar

