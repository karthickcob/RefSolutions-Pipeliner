def call(){
    def pipeliner = new com.bosch.ExtendedBasePipeliner()
    println pipeliner.execute()
    println pipeliner.setup()
    println pipeliner.setupEnvironment()
    println "test"
    
    pipeliner.setup()
}