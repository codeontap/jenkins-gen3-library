// Load the default properties files used to define product wide settings
def call(
        String sitePropertiesFile,
        String productPropertiesFile
) {
    cleanWs()
    script {
        def siteProperties = readProperties interpolate: true, file: "${sitePropertiesFile}";
        siteProperties.each{ k, v -> env["${k}"] ="${v}" }

        def productProperties = readProperties interpolate: true, file: "${productPropertiesFile}";
        productProperties.each{ k, v -> env["${k}"] ="${v}" }
    }
}
