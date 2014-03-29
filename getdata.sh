#!/bin/bash

mkdir -p data/
cd data/
wget http://www.cs.albany.edu/~sigmod14contest/files/outputDir-1k.zip && unzip outputDir-1k.zip && rm outputDir-1k.zip
wget http://www.cs.albany.edu/~sigmod14contest/files/outputDir-10k.zip && unzip outputDir-10k.zip && rm outputDir-10k.zip
cd ..
mkdir -p queries
cd queries
wget http://www.cs.albany.edu/~sigmod14contest/files/1k-sample-queries1.txt
wget http://www.cs.albany.edu/~sigmod14contest/files/1k-sample-answers1.txt
