def call(Map config = [:]) {
    loadLinuxScript(name: 'helloworld.sh')
    sh 'ls -ltr'
    sh "./helloworld.sh ${config.name} ${config.dayOfWeek}"
}