#!/bin/bash

mkdir -p bin
javac -d bin src/sigmod14/mem/graph/*.java
javac -d bin -cp bin:lib/mapdb-0.9.9.jar src/sigmod14/mem/*.java
cd bin
echo -e "Main-Class: sigmod14.mem.Main\n" > manifest.txt
jar cvfm Sigmod14.jar manifest.txt sigmod14/mem/ sigmod14/mem/graph
mv Sigmod14.jar ..
cd ..