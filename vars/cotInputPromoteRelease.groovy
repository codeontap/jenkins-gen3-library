#!/usr/bin/env groovy

def call( def String propertiesFile ) {

    def defaultProperties = [GENERATION_DEBUG: '', AUTOMATION_DEBUG: '' ]
    productProperties = readProperties interpolate: true, file: propertiesFile, defaults: defaultProperties;

    def basicParameters = input message: 'Please Provide Parameters', ok: 'Start', parameters: [
            string( name: 'RELEASE_IDENTIFIER', description: 'Identifier for the release to be accepted. \n The release identifier is allocated during release preparation.' )
        ]
    env.RELEASE_IDENTIFIER = basicParameters["RELEASE_IDENTIFIER"]    
}