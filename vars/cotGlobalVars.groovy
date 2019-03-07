#!/usr/bin/env groovy

@NonCPS
def createBooleanParameter(String desc, String value){

   return [$class: 'BooleanParameterDefinition', defaultValue: true, description: desc, name: value]
}