def call(Map config = [:]) {
    loadLinuxScript(name: 'helloworld.sh')
    sh "echo inside main function"
    sh "echo ${WORKSPACE}"
    sh "${WORKSPACE}/helloworld.sh ${config.name} ${config.dayOfWeek}"
}