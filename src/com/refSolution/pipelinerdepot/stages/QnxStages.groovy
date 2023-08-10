package com.refSolution.pipelinerdepot.stages

import com.bosch.pipeliner.LoggerDynamic
import com.bosch.pipeliner.ScriptUtils
import com.refSolution.pipelinerdepot.stages.CommonStages
import groovy.time.TimeCategory
import groovy.time.TimeDuration
import org.jfrog.hudson.pipeline.common.types.buildInfo.BuildInfo
import groovy.json.JsonSlurper
import groovy.json.JsonOutput


/**
* Contains stages that can be reused across pipelines
*/
class QnxStages {

    private def script
    private Map env
    private LoggerDynamic logger
    private ScriptUtils utils
    private CommonStages commonStages

    /**
     * Constructor
     *
     * @param script Reference to the Jenkins scripted environment
     * @param env Map of Jenkins environment files
     */
    QnxStages(script, Map env) {
        this.script = script
        this.env = env
        this.logger = new LoggerDynamic(script)
        this.utils = new ScriptUtils(script, env)
        this.commonStages = new CommonStages(script, env)
    }

    def stageCheckout(Map env, Map stageInput = [:]) {
        script.stage("Checkout") {
            checkout(env, stageInput)
        }
    }

    def checkout(Map env, Map stageInput = [:]) {
        ArrayList extensions = [
            [$class: 'RelativeTargetDirectory', relativeTargetDir: 'qnx-hv-nxp-s32g']
        ]

        utils.checkout("https://github.boschdevcloud.com/exmachina/qnx-hv-nxp-s32g.git","feature/VIP-648718-PFE-QNX-1.2","hari-user-github",extensions)  
    }
    
}
