#!/usr/bin/env groovy

def call(def String propertiesFile, def List<String> applicationDeploymentUnits) {
    
    def defaultProperties = [GENERATION_DEBUG: '', AUTOMATION_DEBUG: '' ]
    productProperties = readProperties interpolate: true, file: propertiesFile, defaults: defaultProperties;

    def basicParameters = input message: 'Please Provide Parameters', ok: 'Start', parameters: [
            text( name: 'DEPLOYMENT_UNITS', defaultValue: applicationDeploymentUnits.join('\n'), description: "Units to be updated as part of the release preparation. For those units where code references are to be updated, append the detail after the affected unit." ),
            string( name: 'RELEASE_IDENTIFIER', defaultValue: "${env.BUILD_NUMBER}", description: 'Identifier for the release. If not provided, the current build number will be used.' )
        ]
    env.DEPLOYMENT_UNIT_LIST = basicParameters["DEPLOYMENT_UNITS"].split('\n').join(';')
    env.RELEASE_IDENTIFIER = basicParameters["RELEASE_IDENTIFIER"]    
}
