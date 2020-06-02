def call(
    String inputId,
    String heading,
    String message,
    String channel
) {
    def inputUrl = "<${BUILD_URL}/console|Provide Results to Jenkins Pipeline>"
    sendSlackMessage("*Input Required* - ${heading}", "${message} \n ${inputUrl}", 'info', "${channel}" )
}
