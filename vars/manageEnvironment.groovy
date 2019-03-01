#!/usr/bin/env groovy

def call( String status ) {

    def contextProperties = readProperties interpolate: true, file: 'context.properties'

    withEnv( contextProperties ) {
        sh '''#!/bin/bash
        trap \'exit ${RESULT:-1}\' EXIT SIGHUP SIGINT SIGTERM
        ${AUTOMATION_BASE_DIR}/setContext.sh
        RESULT=$?
        ''' 
    }

    contextProperties = readProperties interpolate: true, file: 'context.properties'

    withEnv( contextProperties ) {
        sh '''#!/bin/bash
        trap \'exit ${RESULT:-1}\' EXIT SIGHUP SIGINT SIGTERM
        ${AUTOMATION_DIR}/constructTree.sh
        RESULT=$?
        ''' 
    }

    contextProperties = readProperties interpolate: true, file: 'context.properties'

    withEnv( contextProperties ) {
        sh '''#!/bin/bash
        trap \'exit ${RESULT:-1}\' EXIT SIGHUP SIGINT SIGTERM
        ${AUTOMATION_DIR}/manageEnvironment.sh
        RESULT=$?
        '''
    }
}