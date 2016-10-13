#!/bin/bash
binary_path=`dirname $0`/..
cd ${binary_path}
java -jar maki.jar private/application.properties private/twitter_conf.json
