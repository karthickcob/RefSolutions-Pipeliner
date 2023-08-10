package com.refSolution.pipelinerdepot.pipelines

import com.bosch.pipeliner.BasePipeline
import com.refSolution.pipelinerdepot.stages.CommonStages
import com.refSolution.pipelinerdepot.stages.QnxStages


class QnxPipeline extends BasePipeline {
    CommonStages commonStages
    QnxStages qnxStages
    
    Boolean skipPipeline = false

    QnxPipeline(script, Map env, Map ioMap) {
        super(script, [
            // the input keys and their default values for the pipeline, can be
            // overridden by user inputs from either MR message or Jenkins env
            defaultInputs: '''
                archive = false
                scm_checkout_dir = qnx-hv-nxp-s32g
                submodules_depth = 0
            ''',
            // the keys exposed to the user for modification
            exposed: [
                'archive'
            ],
            // the keys for which pipeline should be parallelized
            parallel: []
        ] as Map, env, ioMap)

        // Specify the node label expression
        // Looks like we can't use && syntax due to input parser
        nodeLabelExpr = "windows-lab-pc"

        commonStages = new CommonStages(script, env)
        qnxStages = new QnxStages(script, env)
    }

    // /**
    // * Provides implementation for stages
    // *
    // * @param A Map with the inputs for stages
    // */
    @Override
    void stages(Map stageInput) {
        // Skip the entire pipeline if we promote and there are no changes
        if (skipPipeline) {
            return
        }

        logger.info("stageInput")
        logger.info(stageInput.inspect())

        commonStages.stageCheckout(env, stageInput)
        // commonStages.sonar(env, stageInput)
        // qnxStages.copyPFE(env, stageInput)
        // qnxStages.setupEnv(env, stageInput)
        // qnxStages.build(env, stageInput)
        // commonStages.notification(env, stageInput)
    }
}