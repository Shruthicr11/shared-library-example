def call(Map config = [:]) { 
  def scriptcontents = libraryResource "com/shruthi/scripts/${config.name}"  
  sh "echo ${WORKSPACE}"  
  writeFile file: "${config.name}", text: scriptcontents 
  sh "chmod a+x ./${config.name}"
} 