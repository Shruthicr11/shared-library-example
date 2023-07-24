def call(Map config = [:]) {
    loadLinuxScript(name: 'helloworld.sh')
    sh "./hello-world.sh ${config.name} ${config.dayOfWeek}"
}