// Keeps a continuous update for the job build Decsription
def call( String descriptionNote ) {
    script {
        env['BUILD_DESCRIPTION'] = "${env['BUILD_DESCRIPTION'] ?: ''} ${descriptionNote} <br> "
    }
    buildDescription("${env["BUILD_DESCRIPTION"]}")
}
