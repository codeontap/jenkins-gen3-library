#!/usr/bin/env groovy

def call( String status ) {

    sh '''#!/bin/bash
    trap \'exit ${RESULT:-1}\' EXIT SIGHUP SIGINT SIGTERM
    ${AUTOMATION_BASE_DIR}/setContext.sh
    RESULT=$?
    ''' 

    sh 'env'

    def contextProperties = readProperties interpolate: true, file: 'context.properties'

    withEnv( contextProperties.collect { /$it.key=$it.value/ } ) {

        sh '''#!/bin/bash
        trap \'exit ${RESULT:-1}\' EXIT SIGHUP SIGINT SIGTERM
        ${AUTOMATION_DIR}/constructTree.sh
        RESULT=$?
        ''' 
        
        sh 'env'
    }

    contextProperties = readProperties interpolate: true, file: 'context.properties'

    withEnv( contextProperties.collect { /$it.key=$it.value/ } ) {
        sh '''#!/bin/bash
        trap \'exit ${RESULT:-1}\' EXIT SIGHUP SIGINT SIGTERM
        ${AUTOMATION_DIR}/manageEnvironment.sh
        RESULT=$?
        '''
    }
}