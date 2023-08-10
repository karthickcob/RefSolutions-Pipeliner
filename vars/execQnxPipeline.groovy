import com.refSolution.pipelinerdepot.pipelines.QnxPipeline

import com.bosch.pipeliner.*

/**
 * execQnxPipeline
 * The script is used to run QnxPipeline.
 * It can be invoked from a Jenkinsfile by writing 'execQnxPipeline()'
 *
 * @author Toni Kauppinen
 */
def call(Map stageOverriders=[:]) {
    Map ioMap = [:]
    Map environment = env.getEnvironment()

    // Override variables from Jenkinsfile with parameter values defined in the job
    if (params) {
        params.each { k, v ->
            environment[k] = v.toString()
        }
    }

    // Run the pipeline
    QnxPipeline pipeline = new QnxPipeline(this, environment, ioMap)
    ioMap = pipeline.run()
}
