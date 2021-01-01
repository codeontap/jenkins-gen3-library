// Support for the "streams" convention to integrate hamlet with Jenkinsfiles
//
// The basic idea is that build pipelines know how to build things and what
// deployment "stream" to inject the build results into depending on the build trigger
// condition. However builds know nothing of environments as such.
//
// Stream pipelines then define the deployment journey of builds through environments.
// Each environment can have an entry and exit "gate", which define the actions needed
// for builds to be deployed into an environment (entry) and for the builds to be
// considered suitable to proceed to the next environment (exist). Depending on
// the conditions included in the gates, a variety of deplyoment patterns, including
// continuous deployment and managed releases can be implemented.
//
// Streams are designed to work with agent none, meaning they run on the lightweight executor
// and thus don't consume resources while waiting for user input - a typical condition used
// in a gate.
//
// The stream convention provides a nice separation between the CI part of the world
// controlled via a Jenkinsfile in a code repo, and the CD part of the world controlled
// by a Jenkinsfile kept in the hamlet CMDB repo. Developers are in full control of what
// happens in their builds, while the change management folks worry about the desired processes
// to get the code into production.
//
// This file provides a range of support routines to assist in the implementation of the
// streams convention.


// Load a product CMDB to get access to the stream pipelines
def loadCMDB( product_cmdb_url = '' ) {

    // Product Setup
    dir('.hamlet/product') {
        script {
            if (product_cmdb_url == '') {
                // CMDB is configured as part of the pipeline
                // Skip the default commit so we can force the value
                // of GIT_COMMIT in the environment when updating build
                // references.
                checkout scm
            } else {
                // Need to load CMDB - don't include it in changelog calculations
                git(
                    url: product_cmdb_url,
                    credentialsId: 'github',
                    changelog: false,
                    poll: false
                )
            }
        }
    }
}

// Load properties defined in the CMDB into the environment
def loadProperties( properties_file ) {

    // Product Setup
    dir('.hamlet/product') {
         // Load in the properties file from the cmdb
        script {
            def contextProperties = readProperties interpolate: true, file: "pipelines/properties/${properties_file}.properties";
            contextProperties.each{ k, v -> env["${k}"] ="${v}" }
        }
    }
}

// Send notifications to whatever channels are required. Currently slack and Teams are supported
def notifyChannels( title, message, channels, colour ) {
    script {
        def channelsList = channels.split(",")

        channelsList.each {
            def channel = it.trim()
            if (channel.startsWith("#")) {
                slackSend (
                    message: "*${title}* | ${BUILD_DISPLAY_NAME} (<${BUILD_URL}|${JOB_NAME}>)  \n${message}",
                    channel: channel,
                    color: colour
                )
            }

            if (channel.startsWith("https://outlook.office.com/webhook/")) {
                // Standard Office 365 format includes JOB NAME and BUILD_URL
                office365ConnectorSend (
                    message: "**${title}** | ${BUILD_DISPLAY_NAME}  \n${message}",
                    webhookUrl: channel,
                    color: colour
                )
            }
        }
    }
}

// Notify of a successful result
def notifySuccess( title, message, channels ) {
    notifyChannels (
        title,
        message,
        channels,
        "#50C878"
    )
}

// Notify of an unsuccessful result
def notifyFailure( title, message, channels ) {
    notifyChannels (
        title,
        message,
        channels,
        "#B22222"
    )
}

// Notify of entry of build into an environment
void notifyEntryConfirmation( approver, environment, segment, group, deploymentUnits, codeUnits, commit, tag, release, channels ) {
    notifySuccess(
        "Entry to environment confirmed",
        "Approver: ${approver}  \nEnvironment: ${environment}  \nSegment: ${segment}  \nGroup: ${group}  \nDeployment Units: ${deploymentUnits}  \nCode Units: ${codeUnits}  \nCommit: ${commit}  \nTag: ${tag}  \nRelease: ${release}",
        channels
    )
}

// Notify of exit of build from an environment
void notifyExitConfirmation( approver, environment, segment, group, deploymentUnits, codeUnits, commit, tag, release, channels ) {
    notifySuccess(
        "Exit from environment confirmed",
        "Approver: ${approver}  \nEnvironment: ${environment}  \nSegment: ${segment}  \nGroup: ${group}  \nDeployment Units: ${deploymentUnits}  \nCode Units: ${codeUnits}  \nCommit: ${commit}  \nTag: ${tag}  \nRelease: ${release}",
        channels
    )
}

// Notify of failure of update during plan preparation
void notifyPlanUpdateFailure( environment, segment, group, units, commit, tag, channels ) {
    notifyFailure(
        "Plan Update Failed",
        "Environment: ${environment}  \nSegment: ${segment}  \nGroup: ${group}  \nUnits: ${units}  \nCommit: ${commit}  \nTag: ${tag}",
        channels
    )
}

// Notify of failure of deploy during plan preparation
void notifyPlanDeployFailure( environment, segment, group, units, channels ) {
    notifyFailure(
        "Plan Deploy Failed",
        "Environment: ${environment}  \nSegment: ${segment}  \nGroup: ${group}  \nUnits: ${units}",
        channels
    )
}

// Notify of transfer of builds between environments
// Only needed where environments don't share registries
def notifyTransferSuccess( fromEnvironment, environment, segment, group, units, commit, channels ) {
    notifySuccess(
        "Transfer Completed",
        "FromEnvironment: ${fromEnvironment}  \nEnvironment: ${environment}  \nSegment: ${segment}  \nGroup: ${group}  \nUnits: ${units}  \nCommit: ${commit}",
        channels
    )
}

// Notify of failure of transfer of builds between environments
def notifyTransferFailure( fromEnvironment, environment, segment, group, units, commit, channels ) {
    notifyFailure(
        "Transfer Failed",
        "FromEnvironment: ${fromEnvironment}  \nEnvironment: ${environment}  \nSegment: ${segment}  \nGroup: ${group}  \nUnits: ${units}  \nCommit: ${commit}",
        channels
    )
}

// Notify of updates to build references
def notifyUpdateSuccess( environment, segment, group, units, commit, tag, channels ) {
    notifySuccess(
        "Update References Completed",
        "Environment: ${environment}  \nSegment: ${segment}  \nGroup: ${group}  \nUnits: ${units}  \nCommit: ${commit}  \nTag: ${tag}",
        channels
    )
}

// Notify of failure of updates to build references
def notifyUpdateFailure( environment, segment, group, units, commit, tag, channels ) {
    notifyFailure(
        "Update References Failed",
        "Environment: ${environment}  \nSegment: ${segment}  \nGroup: ${group}  \nUnits: ${units}  \nCommit: ${commit}  \nTag: ${tag}",
        channels
    )
}

// Notify of deployment to an environment
def notifyDeploySuccess( environment, segment, group, units, release, channels ) {
    notifySuccess(
        "Deploy Completed",
        "Environment: ${environment}  \nSegment: ${segment}  \nGroup: ${group}  \nUnits: ${units}  \nRelease: ${release}",
        channels
    )
}

// Notify of failure of a deployment to an environment
def notifyDeployFailure( environment, segment, group, units, release, channels ) {
    notifyFailure(
        "Deploy Failed",
        "Environment: ${environment}  \nSegment: ${segment}  \nGroup: ${group}  \nUnits: ${units}  \nRelease: ${release}",
        channels
    )
}

// Notify of acceptance of an environment
def notifyAcceptSuccess( environment, segment, group, units, release, channels ) {
    notifySuccess(
        "Accept Completed",
        "Environment: ${environment}  \nSegment: ${segment}  \nGroup: ${group}  \nUnits: ${units}  \nRelease: ${release}",
        channels
    )
}

// Notify of failure of acceptance of an environment
def notifyAcceptFailure( environment, segment, group, units, release, channels ) {
    notifyFailure(
        "Accept Failed",
        "Environment: ${environment}  \nSegment: ${segment}  \nGroup: ${group}  \nUnits: ${units}  \nRelease: ${release}",
        channels
    )
}
