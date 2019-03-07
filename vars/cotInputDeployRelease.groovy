#!/usr/bin/env groovy

def call( ) {

    def deploymentMode = env.DEPLOYMENT_MODE ?: 'update,stopstart,stop'

    def basicParameters = input message: 'Please Provide Parameters', ok: 'Start', parameters: [
            choice(name: 'MODE', choices: "${deploymentMode.split(",").join("\n")}", description: '''
                Desired way in which deploy should occur. 
                    "update" will attempt a hot cutover from the running deployment to the desired deployment.
                    "stopstart" will first stop the running deployment before starting the desired deployment.
                    "stop" will stop the running deployment but not start the desired deployment - mainly intended to facilitate database maintenance without the application.
                ''' ),
            string( name: 'RELEASE_IDENTIFIER', defaultValue: '', description: 'Identifier for the release to be deployed. \n The release identifier is allocated during release preparation.' )
        ]

    env.RELEASE_IDENTIFIER = basicParameters["RELEASE_IDENTIFIER"]    

     // Deployment Units
    def applicationUnitParameters = []
    env.APPLICATION_UNITS.split(",").each {
        applicationUnitParameters += cotInput.createBooleanParameter( '', it)
    }
    def appliationUnitInputs = input(

        id: 'userInput', message: 'Deployment Units - One or more units to deploy', parameters: applicationUnitParameters
    )
    def applicationUnits=[]
    appliationUnitInputs?.findAll{ it.value }?.each {
        applicationUnits += [ it.key.toString() ]
    }
    env.DEPLOYMENT_UNITS = applicationUnits.join(",")
}