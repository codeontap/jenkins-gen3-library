// Send a slack message with a fixed format and fixed status colours
def call( String heading, String message, String color, String channel ) {
    def slackColours = [ 'good' : '#50C878', 'bad' : '#B22222', 'info' : '#62B1F6' ]

    slackSend (
        message: "${heading} - ${BUILD_DISPLAY_NAME} (<${BUILD_URL}|Open>)\n${message}",
        channel: "${channel}",
        color: "${slackColours["${color}"]}"
    )
}
