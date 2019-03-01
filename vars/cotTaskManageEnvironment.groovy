#!/usr/bin/env groovy

def call( 
        String propertiesFile, 
        String deploymentMode, 
        String environment, 
        String segment, 
        String comment, 
        String treatRunIdAsSignificant,
        String levelsList,
        String segmentUnitsList,
        String solutionUnitsList,
        String applicationUnitsList ) {

    def environmentVariables = [ 
        "ENVIRONMENT=$environment", 
        "DEPLOYMENT_MODE=$deploymentMode",
        "SEGMENT=$segment",
        "COMMENT=$comment",
        "TREAT_RUN_ID_AS_SIGNIFICANT=$treatRunIdAsSignificant",
        "LEVELS_LIST=$levelsList",
        "SEGMENT_UNITS_LIST=$segmentUnitsList",
        "SOLUTION_UNITS_LIST=$solutionUnitsList",
        "APPLICATION_UNITS_LIST=$applicationUnitsList" ]

    def productProperties = readProperties interpolate: true, file: propertiesFile;
    environmentVariables += productProperties.collect {/$it.key=$it.value/ }

    sh '''#!/bin/bash
    trap \'exit ${RESULT:-1}\' EXIT SIGHUP SIGINT SIGTERM
    ${AUTOMATION_BASE_DIR}/setContext.sh
    RESULT=$?
    ''' 

    def contextProperties = readProperties interpolate: true, file: 'context.properties'
    environmentVariables += contextProperties.collect {/$it.key=$it.value/ }

    withEnv( environmentVariables ) {

        sh 'env'
        
        sh '''#!/bin/bash
        trap \'exit ${RESULT:-1}\' EXIT SIGHUP SIGINT SIGTERM
        ${AUTOMATION_DIR}/constructTree.sh
        RESULT=$?
        ''' 
    }

    contextProperties = readProperties interpolate: true, file: 'context.properties'
    environmentVariables += contextProperties.collect {/$it.key=$it.value/ }

    withEnv( environmentVariables ) {
        sh '''#!/bin/bash
        trap \'exit ${RESULT:-1}\' EXIT SIGHUP SIGINT SIGTERM
        ${AUTOMATION_DIR}/manageEnvironment.sh
        RESULT=$?
        '''
    }
}