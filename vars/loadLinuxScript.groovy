def call(Map config = [:]) {
    def scriptcontents = libraryResource "com/shruthi/scripts/${config.name}"
    writeFile file: "${config.name}", text: scriptcontents
    sh 'chmod +x ./${config.name}'
}