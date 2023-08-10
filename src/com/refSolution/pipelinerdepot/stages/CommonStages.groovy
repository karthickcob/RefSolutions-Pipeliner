
package com.refSolution.pipelinerdepot.stages

import com.bosch.pipeliner.LoggerDynamic
import com.bosch.pipeliner.ScriptUtils

/**
* Contains stages that can be reused across pipelines
*/
class CommonStages {

    /**
     * for shallow clone using gitlab_branch_source plugin, the minimum depth
     * has to be at least the number of commits in the MR + 1, so the latest
     * commit from main branch is also fetched, and the MR can be merged into it.
     * in case the value is not sets, perform a full clone
     */
    final int defaultShallowCloneDepth = -1

    private def script
    private Map env
    private LoggerDynamic logger
    private ScriptUtils utils

    /**
     * Constructor
     *
     * @param script Reference to the Jenkins scripted environment
     * @param env Map of Jenkins environment files
     */
    CommonStages(script, Map env) {
        this.script = script
        this.env = env
        this.utils = new ScriptUtils(script, env)
        this.logger = new LoggerDynamic(script)
    }

    Boolean isAllSet(List<String> variables) {
        for (variable in variables) {
            if (!isSet(variable)) {
                return false
            }
        }
        return true
    }

    Boolean isAnySet(List<String> variables) {
        for (variable in variables) {
            if (isSet(variable)) {
                return true
            }
        }
        return false
    }

    Boolean isSet(String variable) {
        return variable?.trim()
    }

    Boolean isSet(List variable) {
        return variable != null && variable.size() > 0
    }

    Boolean isTrue(String variable) {
        return isSet(variable) && variable.equalsIgnoreCase('true')
    }

    /**
     * Checks out the associated git repository and initializes and
     * updates any submodules contained in the repo.
     *
     * @param env Map of Jenkins environment files
     * @param stageInput Map of input parameters
     */
    def stageCheckout(Map env, Map stageInput = [:]) {
        script.stage("Checkout") {
            checkout(env, stageInput)
        }
    }

    /**
     * Creates the GitSCM extensions object based on defaults and stageInput
     *
     * @param stageInput Map of stage input parameters
     */
    private ArrayList getSCMExtensions(Map stageInput) {
        // if 'submodules_shallow' is enabled and 'submodules_depth' is not specified, defaults to 1
        // this will be ignored if 'submodules_shallow' is set to 'false'
        int submodules_depth = isSet(stageInput.submodules_depth) ? stageInput.submodules_depth as int : 1
        boolean submodules_shallow = isTrue(stageInput.submodules_shallow)
        boolean submodules_disable = isTrue(stageInput.submodules_disable)
        boolean submodules_recursive = isTrue(stageInput.submodules_recursive)

        String scm_checkout_dir = stageInput.custom_scm_checkout_dir?.trim() ?: ''

        /**
         * if 'clone_shallow' is enabled and 'clone_depth' is not specified,
         * defaults to the value of 'defaultShallowCloneDepth'.
         * This will configuration will be ignored if 'clone_shallow' is set to 'false'
         */
        int clone_depth = isSet(stageInput.clone_depth) ? stageInput.clone_depth as int : defaultShallowCloneDepth
        boolean clone_shallow = isTrue(stageInput.clone_shallow)
        boolean clone_no_tags = isTrue(stageInput.clone_no_tags)
        String clone_reference = stageInput.clone_reference?.trim() ?: null

        ArrayList extensions = [

            [$class: 'CloneOption',
            shallow: clone_shallow,
            depth: clone_depth,
            noTags: clone_no_tags,
            reference: clone_reference],

            [$class: 'RelativeTargetDirectory',
            relativeTargetDir: scm_checkout_dir]
        ]

        logger.info("submodules_disable : " + submodules_disable)
        logger.info("submodules_recursive : " + submodules_recursive)
        logger.info("submodules_shallow : " + submodules_shallow)
        logger.info("submodules_depth : " + submodules_depth)
        logger.info("clone_shallow : " +  clone_shallow)
        logger.info("clone_depth : " + clone_depth)
        logger.info("clone_reference : " + clone_reference)
        return extensions
    }

    /**
     * Checkout without a stage
     *
     *
     * @param env Map of Jenkins environment files
     * @param stageInput Map of input parameters
     */
    def checkout(Map env, Map stageInput = [:]) {
        String branch = env.CHECKOUT_BRANCH ?: "master"
        String checkoutCredentialsId = env.CHECKOUT_CREDENTIALS_ID ?: ""
        ArrayList extensions = getSCMExtensions(stageInput)
        logger.info('CHECKOUT COMMON STAGE')
        utils.checkout(env.CHECKOUT_URL, branch, checkoutCredentialsId, extensions)
    }

    /**
     * Captures the files built matching the included pattern and saves them to the
     * Jenkins master as build artifacts.
     *
     * @param patterns List of pattern strings that should be saved
     */
    def stageArchive(ArrayList<String> patterns) {
        script.stage("Archive") {
            for (pattern in patterns) {
                script.archiveArtifacts pattern
            }
        }
    }

    /**
    * Triggers cleanup of the workspace after the job finishes.
    * For cleanup during the run, use Jenkins function deleteDir()
    */
    def stageCleanup() {
        script.stage("Cleanup") {
            script.step([$class: 'WsCleanup'])
        }
    }
}
