#!/usr/bin/env groovy

def call( String propertiesFile ) {

    def defaultProperties = [DEPLOYMENT_MODE_LIST: 'update,stop', LEVELS_LIST: 'segment,solution,application', GENERATION_DEBUG: '', AUTOMATION_DEBUG: '' ]
    productProperties = readProperties interpolate: true, file: propertiesFile, defaults: defaultProperties;

    def basicParameters = input message: 'Please Provide Parameters', ok: 'Start', parameters: [
            choice(name: 'DEPLOYMENT_MODE', choices: "${productProperties["DEPLOYMENT_MODE_LIST"].split(",").join("\n")}", description: 'Desired way in which deploy should occur' ),
            choice(name: 'ENVIRONMENT', choices: "${productProperties["ENVIRONMENT_LIST"].split(",").join("\n")}", description: 'Environment to manage'),
            choice(name: 'SEGMENT', choices: "${productProperties["SEGMENT_LIST"].split(",").join("\n")}", description: 'Segment to manage' ),
            string(name: 'COMMENT', defaultValue: '', description: 'Added to the git commit message' ),
            booleanParam( name: 'TREAT_RUN_ID_DIFFERENCES_AS_SIGNIFICANT', defaultValue: false, description: 'Set this to force redeployment where only the runid value has changed. Mainly used where data is in S3.')
        ]

    env.DEPLOYMENT_MODE = basicParameters["DEPLOYMENT_MODE"]
    env.ENVIRONMENT = basicParameters["ENVIRONMENT"]
    env.SEGMENT = basicParameters["SEGMENT"]
    env.COMMENT = basicParameters["COMMENT"]
    env.TREAT_RUN_ID_DIFFERENCES_AS_SIGNIFICANT = basicParameters["TREAT_RUN_ID_DIFFERENCES_AS_SIGNIFICANT"]

    // Levels
    def levelParameters = []
    productProperties["LEVELS_LIST"].split(",").each { 
        levelParameters += cotInput.createBooleanParameter('', it )
    }
    def levelInputs = input(

        id: 'userInput', message: 'Component Levels', parameters: levelParameters
    )
    def levels = []
    levelInputs?.findAll{ it.value }?.each {
        levels += [ it.key.toString() ]
    }
    env.LEVELS_LIST = levels.join(",")
    
    // Segment units
    if env.LEVELS_LIST.contains('segment') {  
        def segmentUnitParameters = []
        productProperties["SEGMENT_UNITS"].split(",").each {
            segmentUnitParameters += cotInput.createBooleanParameter('', it)
        }
        def segmentUnitInputs = input(

            id: 'userInput', message: 'Segment Level Units', parameters: segmentUnitParameters
        )
        def segmentUnits=[]
        segmentUnitInputs?.findAll{ it.value }?.each {
            segmentUnits += [ it.key.toString() ]
        }
        env.SEGMENT_UNITS = segmentUnits.join(",")
    }

    // Solution Units
    if env.LEVELS_LIST.contains('solution') {
        def solutionUnitParameters = []
        productProperties["SOLUTION_UNITS"].split(",").each {
            solutionUnitParameters += cotInput.createBooleanParameter('', it)
        }
        def solutionUnitInputs = input(

            id: 'userInput', message: 'Solution Level Units', parameters: solutionUnitParameters
        )
        def solutionUnits=[]
        solutionUnitInputs?.findAll{ it.value }?.each {
            solutionUnits += [ it.key.toString() ]
        }
        env.SOLUTION_UNITS = solutionUnits.join(",")
    }

    // Application Units
    if env.LEVELS_LIST.contains('application') {
        def applicationUnitParameters = []
        productProperties["APPLICATION_UNITS"].split(",").each {
            applicationUnitParameters += cotInput.createBooleanParameter( '', it)
        }
        def appliationUnitInputs = input(

            id: 'userInput', message: 'Application Level Units', parameters: applicationUnitParameters
        )
        def applicationUnits=[]
        appliationUnitInputs?.findAll{ it.value }?.each {
            applicationUnits += [ it.key.toString() ]
        }
        env.APPLICATION_UNITS = applicationUnits.join(",")
    }

}