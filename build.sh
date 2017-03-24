#!/usr/bin/env bash

mvn clean install
cp target/batUtils-1.3-full.jar ~/batclient/plugins/
/Users/axu/bat/Batclient.app/Contents/MacOS/batclient