#!/usr/bin/env groovy

def call( String propertiesFile, String agentLabel ) {

    def productProperties 
    pipeline { 
        agent { 
            label "${agentLabel}"
        }
        stages {
            stage('ManageEnvironment') {
                script {
                    productProperties = readProperties interpolate: true, file: propertiesFile;
                }
                input {
                    message 'Please Provide Parameters'
                    ok 'Start'
                    parameters {
                        choice(name: 'DEPLOYMENT_MODE', choices: "${propertiesFile["DEPLOYMENT_MODE_LIST"].split(',').join('\n')}", description: 'Select the deployment mode')
                        choice(name: 'ENVIRONMENT', choices: "${propertiesFile["ENVIRONMENT_LIST"].split(',').join('\n')}", description: 'Select the Environment to manage')
                        choice(name: 'SEGMENT', choices: "${propertiesFile["SEGMENT_LIST"].split(',').join('\n')}", description: 'Select the Segment to manage')
                        string(name: 'COMMENT', defaultValue: '', description: 'Added to the git commit message' )
                        booleanParam( name: 'TREAT_RUN_ID_DIFFERENCES_AS_SIGNIFICANT', defaultValue: false, description: 'Set this to force redeployment where only the runid value has changed. Mainly used where data is in S3.')
                    }
                }
                steps {
                    sh 'env'
                    manageEnvironment propertiesFile
                }
            }
        }
    }
}