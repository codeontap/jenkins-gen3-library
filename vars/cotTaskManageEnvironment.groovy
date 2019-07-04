#!/usr/bin/env groovy

def call( 
        String      environment, 
        String      segment, 
        String      deploymentMode,
        String      comment ,  
        String[]    levelsList,
        String[]    segmentUnits,
        String[]    solutionUnits,
        String[]    applicationUnits,
        Boolean     runIdSignificant = false ) {

    skipDefaultCheckout(true)

    levelsList          = levelsList.join(',')
    segmentUnits        = segmentUnits.join(',')
    solutionUnits       = solutionUnits.join(',')
    applicationUnits    = applicationUnits.join(',')
    runIdSignificant    = runIdSignificant as String

    def environmentVariables = []

    environmentVariables['ENVIRONMENT'] = environment ?: env.ENVIRONMENT
    environmentVariables['SEGMENT'] = segment ?: env.SEGMENT
    environmentVariables['deploymentMode'] = deploymentMode ?: env.DEPLOYMENT_MODE
    environmentVariables['comment'] = deploymentMode ?: env.COMMENT
    environmentVariables['LEVELS_LIST'] = levelsList ?: env.LEVELS_LIST
    environmentVariables['SEGMENT_UNITS_LIST'] = segmentUnits ?: env.SEGMENT_UNITS
    environmentVariables['SOLUTION_UNITS_LIST'] =  solutionUnits ?: env.SOLUTION_UNITS
    environmentVariables['APPLICATION_UNITS_LIST'] = applicationUnits ?: env.APPLICATION_UNITS
    environmentVariables['TREAT_RUN_ID_DIFFERENCES_AS_SIGNIFICANT'] = runIdSignificant ?: env.TREAT_RUN_ID_DIFFERENCES_AS_SIGNIFICANT

    def siteProperties = readProperties interpolate: true, file: cot.siteProperties();
    environmentVariables += siteProperties.collect {/$it.key=$it.value/ }

    withEnv ( environmentVariables ) {
        sh '''#!/bin/bash
            trap \'exit ${RESULT:-1}\' EXIT SIGHUP SIGINT SIGTERM
            ${AUTOMATION_BASE_DIR}/setContext.sh
            RESULT=$?
        '''
    }

    def contextProperties = readProperties interpolate: true, file: 'context.properties'
    environmentVariables += contextProperties.collect {/$it.key=$it.value/ }

    withEnv( environmentVariables ) {
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