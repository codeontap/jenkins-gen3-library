def call( String releasePrefix ) {
    script {


        if( "${env["BUILD_DISPLAY_NAME"]}" == "${env["BUILD_NUMBER"]}" ) {
            env['RELEASE_NAME'] =  "${releasePrefix}_${env["BUILD_NUMBER"]}"
        }
        else {
            def String cleanReleaseName = "${env["BUILD_DISPLAY_NAME"].replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit}]", ""); }"
            env['RELEASE_NAME'] =  "${releasePrefix}_${cleanReleaseName}_${env["BUILD_NUMBER"]}"
        }
    }
}
