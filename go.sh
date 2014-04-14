#!/bin/bash

rm -f Sigmod14.jar ../all.tar.gz
cd src
./compile.sh
cd ..
tar -czvf ../all.tar.gz Sigmod14.jar run.sh src