#!/usr/bin/env groovy

def call( String status ) {

sh '''#!/bin/bash
trap \'exit ${RESULT:-1}\' EXIT SIGHUP SIGINT SIGTERM
${AUTOMATION_BASE_DIR}/setContext.sh
RESULT=$?
''' 

sh '''#!/bin/bash
trap \'exit ${RESULT:-1}\' EXIT SIGHUP SIGINT SIGTERM
${AUTOMATION_DIR}/constructTree.sh
RESULT=$?
''' 

sh '''#!/bin/bash
trap \'exit ${RESULT:-1}\' EXIT SIGHUP SIGINT SIGTERM
${AUTOMATION_DIR}/manageEnvironment.sh
RESULT=$?
'''
}