#!/usr/bin/env bash
#!/bin/bash

BASEDIR=$(dirname "$0")

mkdir -p $1/{report,locators/{pages,component},scenarios/test-sample,data/{patches,variations,credentials,javascript,shell,csv,excel}}
cp $BASEDIR/global-config-example.xml $1

cp $BASEDIR/scenario.xml $1/scenarios/test-sample
cp $BASEDIR/*.json $1/scenarios/test-sample
cp $BASEDIR/*.js $1/data/javascript
cp $BASEDIR/*.sh $1/data/shell
