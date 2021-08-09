javac -d bin -cp "lib/*" src/*
jar cfve EV3Dev.jar App -C bin . -C lib .
