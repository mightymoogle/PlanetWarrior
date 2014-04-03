set "PATH=%PATH%;C:\Program Files\Java\jdk1.7.0_04\bin"
javac *.java
java -jar tools/PlayGame.jar maps/map7.txt 1000 1000 log.txt "java PlanetWarrior" "java -jar example_bots/RageBot.jar" | java -jar tools/ShowGame.jar