#!/usr/bin/env groovy

def call( ) {
    def basicParameters = input message: 'Please Provide Parameters', ok: 'Start', parameters: [
            string( name: 'RELEASE_IDENTIFIER', defaultValue: env.BUILD_NUMBER, description: 'Identifier for the release to be accepted. \n The release identifier is allocated during release preparation.' )
        ]
    env.RELEASE_IDENTIFIER = basicParameters["RELEASE_IDENTIFIER"]    
}