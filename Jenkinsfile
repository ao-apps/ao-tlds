#!/usr/bin/env groovy
/*
 * ao-tlds - Self-updating Java API to get top-level domains.
 * Copyright (C) 2021, 2022, 2023  AO Industries, Inc.
 *     support@aoindustries.com
 *     7262 Bull Pen Cir
 *     Mobile, AL 36695
 *
 * This file is part of ao-tlds.
 *
 * ao-tlds is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ao-tlds is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ao-tlds.  If not, see <https://www.gnu.org/licenses/>.
 */

// Parent, Extensions, Plugins, Direct and BOM Dependencies
def upstreamProjects = [
  // Parent
  'parent', // <groupId>com.aoapps</groupId><artifactId>ao-oss-parent</artifactId>
  // Parent Plugin Dependencies (Avoid cyclic dependency)
  'pgp-keys-map', // <groupId>com.aoapps</groupId><artifactId>pgp-keys-map</artifactId>
  'javadoc-offline', // <groupId>com.aoapps</groupId><artifactId>ao-javadoc-offline</artifactId>
  'javadoc-resources', // <groupId>com.aoapps</groupId><artifactId>ao-javadoc-resources</artifactId>
  'ant-tasks', // <groupId>com.aoapps</groupId><artifactId>ao-ant-tasks</artifactId>
  'checkstyle-config', // <groupId>com.aoapps</groupId><artifactId>ao-checkstyle-config</artifactId>

  // Direct
  'collections', // <groupId>com.aoapps</groupId><artifactId>ao-collections</artifactId>
  'lang', // <groupId>com.aoapps</groupId><artifactId>ao-lang</artifactId>

  // Test Direct
  // No Jenkins: <groupId>junit</groupId><artifactId>junit</artifactId>
]

/******************************************************************************************
 *                                                                                        *
 * Everything below this line is identical for all projects, except the copied matrix     *
 * axes and any "Begin .*custom" / "End .*custom" blocks (see filter_custom script).      *
 *                                                                                        *
 * See https://issues.jenkins.io/browse/JENKINS-61047                                     *
 * See https://issues.jenkins.io/browse/JENKINS-61280                                     *
 *                                                                                        *
 ******************************************************************************************
 *                                                                                        *
 * Variables that may be defined above this block:                                        *
 *                                                                                        *
 * deployJdk            The version of JDK that will be used for the deploy stage.        *
 *                      Defaults to '20'                                                  *
 *                                                                                        *
 * buildJdks            The array of JDK versions that will build.                        *
 *                      Defaults to ['11', '17', '20']                                    *
 *                      Changes must be copied to matrix axes!                            *
 *                                                                                        *
 * testJdks             The array of JDK versions that will test against every build JDK. *
 *                      Defaults to ['11', '17', '20']                                    *
 *                      Changes must be copied to matrix axes!                            *
 *                                                                                        *
 * upstreamProjects     The array of relative paths to upstream projects.                 *
 *                      Defaults to []                                                    *
 *                                                                                        *
 * projectDir           The directory within the workspace containing the Maven project.  *
 *                      Default depends on the path of Jenkinsfile:                       *
 *                          'Jenkinsfile'       -> '.'                                    *
 *                          'book/Jenkinsfile'  -> 'book'                                 *
 *                          'devel/Jenkinsfile' -> 'devel'                                *
 *                                                                                        *
 * abortOnUnreadyDependency  Aborts the build when any dependency is queued, building,    *
 *                           or unsuccessful.                                             *
 *                           Defaults to true                                             *
 *                                                                                        *
 * disableSubmodules    Disables checkout of Git submodules.                              *
 *                      Defaults to true                                                  *
 *                                                                                        *
 * sparseCheckoutPaths  The sparse paths for Git checkout.                                *
 *                      Default depends on projectDir:                                    *
 *                          '.'     -> [[path:'/*'],                                      *
 *                                      [path:'!/book/'],                                 *
 *                                      [path:'!/devel/']]                                *
 *                          'book'  -> [[path:'/.gitignore'],                             *
 *                                      [path:'/.gitmodules'],                            *
 *                                      [path:'/book/']]                                  *
 *                          'devel' -> [[path:'/.gitignore'],                             *
 *                                      [path:'/.gitmodules'],                            *
 *                                      [path:'/devel/']]                                 *
 *                                                                                        *
 * scmUrl               The Git URL.                                                      *
 *                      Default depends on the project's SCM settings:                    *
 *                          scm.userRemoteConfigs[0].url                                  *
 *                                                                                        *
 * scmBranch            The Git branch name, without and ref/path prefix                  *
 *                      Default depends on the project's SCM settings:                    *
 *                          scm.branches[0].name: 'refs/heads/*' -> '*'                   *
 *                                                                                        *
 * scmBrowser           The Git SCM browser URL.                                          *
 *                      Default depends on scmUrl:                                        *
 *                          '/srv/git/ao-apps/*' -> 'https://github.com/ao-apps/*'        *
 *                          '/srv/git/nmwoss/*'  -> 'https://github.com/newmediaworks/*'  *
 *                          '/srv/git/*'         -> No default                            *
 *                                                                                        *
 * buildPriority        The build job priority from 1 to 30 (lower is build first).       *
 *                      Defaults to the depth in the upstreamProjects dependency graph.   *
 *                                                                                        *
 * quietPeriod          The time to delay between queuing job and starting build.         *
 *                      Default depends on buildPriority:                                 *
 *                          10 + buildPriority * 2                                        *
 *                                                                                        *
 * nice                 The nice level to run the build processes or 0 for none.          *
 *                      Default depends on buildPriority:                                 *
 *                          min(19, buildPriority - 1)                                    *
 *                                                                                        *
 * maven                The Maven tool to use.                                            *
 *                      Defaults to 'maven-3'                                             *
 *                                                                                        *
 * mavenOpts            The Maven Java options.                                           *
 *                      Defaults to '-Djansi.force' for colorful logs                     *
 *                                                                                        *
 * mavenOptsJdk16       The Maven Java options for JDK 16+.                               *
 *                      Defaults to exporting Java compiler for rewrite-maven-plugin.     *
 *                                                                                        *
 * extraProfiles        An array of additional profiles to pass to Maven.                 *
 *                      Defaults to []                                                    *
 *                                                                                        *
 * testWhenExpression   A closure determining when to run tests.                          *
 *                      Defaults to {return fileExists(projectDir + '/src/test')}         *
 *                                                                                        *
 * sonarqubeWhenExpression  A closure determining when to perform SonarQube analysis.     *
 *                          Defaults to {                                                 *
 *                            return !fileExists(                                         *
 *                              projectDir + '/.github/workflows/build.yml'               *
 *                            )                                                           *
 *                          }                                                             *
 *                                                                                        *
 * failureEmailTo       The recipient of build failure emails.                            *
 *                      Defaults to 'support@aoindustries.com'                            *
 *                                                                                        *
 *****************************************************************************************/

// If RejectedAccessException, grant per-controller:
//   https://jenkins.aoindustries.com/scriptApproval/
//   "Approve"
//   Repeat until all permissions granted

// JDK versions
if (!binding.hasVariable('deployJdk')) {
  binding.setVariable('deployJdk', '20')
}
if (!binding.hasVariable('buildJdks')) {
  binding.setVariable(
    'buildJdks',
    ['11', '17', '20'] // Changes must be copied to matrix axes!
  )
}
if (!binding.hasVariable('testJdks')) {
  binding.setVariable(
    'testJdks',
    ['11', '17', '20'] // Changes must be copied to matrix axes!
  )
}
if (!binding.hasVariable('upstreamProjects')) {
  binding.setVariable('upstreamProjects', [])
}
if (!binding.hasVariable('projectDir')) {
  def scriptPath = currentBuild.rawBuild.parent.definition.scriptPath
  def projectDir
  if (scriptPath == 'Jenkinsfile') {
    projectDir = '.'
  } else if (scriptPath == 'book/Jenkinsfile') {
    projectDir = 'book'
  } else if (scriptPath == 'devel/Jenkinsfile') {
    projectDir = 'devel'
  } else {
    throw new Exception("Unexpected value for 'scriptPath': '$scriptPath'")
  }
  binding.setVariable('projectDir', projectDir)
}
if (!binding.hasVariable('abortOnUnreadyDependency')) {
  binding.setVariable('abortOnUnreadyDependency', true)
}
if (!binding.hasVariable('disableSubmodules')) {
  binding.setVariable('disableSubmodules', true)
}
if (!binding.hasVariable('sparseCheckoutPaths')) {
  def sparseCheckoutPaths
  if (projectDir == '.') {
    sparseCheckoutPaths = [
      [path:'/*'],
      [path:'!/book/'],
      [path:'!/devel/']
    ]
  } else if (projectDir == 'book' || projectDir == 'devel') {
    sparseCheckoutPaths = [
      [path:'/.gitignore'],
      [path:'/.gitmodules'],
      [path:"/$projectDir/"]
    ]
  } else {
    throw new Exception("Unexpected value for 'projectDir': '$projectDir'")
  }
  binding.setVariable('sparseCheckoutPaths', sparseCheckoutPaths)
}
if (!binding.hasVariable('scmUrl')) {
  // Automatically determine Git URL: https://stackoverflow.com/a/38255364
  if (scm.userRemoteConfigs.size() == 1) {
    binding.setVariable('scmUrl', scm.userRemoteConfigs[0].url)
  } else {
    throw new Exception("Precisely one SCM remote expected: '" + scm.userRemoteConfigs + "'")
  }
}
if (!binding.hasVariable('scmBranch')) {
  // Automatically determine branch
  if (scm.branches.size() == 1) {
    def scmBranchPrefix = 'refs/heads/'
    def scmBranch = scm.branches[0].name
    if (scmBranch.startsWith(scmBranchPrefix)) {
      scmBranch = scmBranch.substring(scmBranchPrefix.length())
      binding.setVariable('scmBranch', scmBranch)
    } else {
      throw new Exception("SCM branch does not start with '$scmBranchPrefix': '$scmBranch'")
    }
  } else {
    throw new Exception("Precisely one SCM branch expected: '" + scm.branches + "'")
  }
}
if (!binding.hasVariable('scmBrowser')) {
  // Automatically determine SCM browser
  def aoappsPrefix        = '/srv/git/ao-apps/'
  def newmediaworksPrefix = '/srv/git/nmwoss/'
  def scmBrowser
  if (scmUrl.startsWith(aoappsPrefix)) {
    // Is also mirrored to GitHub user "ao-apps"
    def repo = scmUrl.substring(aoappsPrefix.length())
    if (repo.endsWith('.git')) {
      repo = repo.substring(0, repo.length() - 4)
    }
    scmBrowser = [$class: 'GithubWeb',
      repoUrl: 'https://github.com/ao-apps/' + repo
    ]
  } else if (scmUrl.startsWith(newmediaworksPrefix)) {
    // Is also mirrored to GitHub user "newmediaworks"
    def repo = scmUrl.substring(newmediaworksPrefix.length())
    if (repo.endsWith('.git')) {
      repo = repo.substring(0, repo.length() - 4)
    }
    scmBrowser = [$class: 'GithubWeb',
      repoUrl: 'https://github.com/newmediaworks/' + repo
    ]
  } else if (scmUrl.startsWith('/srv/git/') || scmUrl.startsWith('ssh://')) {
    // No default
    scmBrowser = null
  } else {
    throw new Exception("Unexpected SCM URL: '$scmUrl'")
  }
  binding.setVariable('scmBrowser', scmBrowser)
}

/*
 * Finds the upstream projects for the given job.
 * Returns an array of upstream projects, possibly empty but never null
 */
def getUpstreamProjects(jenkins, upstreamProjectsCache, workflowJob) {
  def fullName = workflowJob.fullName
  def upstreamProjects = upstreamProjectsCache[fullName]
  if (upstreamProjects == null) {
    upstreamProjects = []
    workflowJob.triggers?.each {triggerDescriptor, trigger ->
      if (trigger instanceof jenkins.triggers.ReverseBuildTrigger) {
        trigger.upstreamProjects.split(',').each {upstreamProject ->
          upstreamProject = upstreamProject.trim()
          if (upstreamProject != '') {
            upstreamProjects << upstreamProject
          }
        }
      } else {
        throw new Exception("$fullName: trigger is not a ReverseBuildTrigger: $upstreamFullName -> $trigger")
      }
    }
    upstreamProjectsCache[fullName] = upstreamProjects
  }
  return upstreamProjects
}

/*
 * Gets the set of full names for the given workflowJob and all transitive upstream projects.
 */
def getAllUpstreamProjects(jenkins, upstreamProjectsCache, allUpstreamProjectsCache, workflowJob) {
  def fullName = workflowJob.fullName
  def allUpstreamProjects = allUpstreamProjectsCache[fullName]
  if (allUpstreamProjects == null) {
    if (allUpstreamProjectsCache.containsKey(fullName)) {
      throw new Exception("$fullName: Loop detected in upstream project graph")
    }
    // Add to map with null for loop detection
    allUpstreamProjectsCache[fullName] = null
    // Create new set
    allUpstreamProjects = [] as Set<String>
    // Always contains self
    allUpstreamProjects << fullName
    // Transitively add all
    for (upstreamProject in getUpstreamProjects(jenkins, upstreamProjectsCache, workflowJob)) {
      def upstreamWorkflowJob = jenkins.getItem(upstreamProject, workflowJob)
      if (upstreamWorkflowJob == null) {
        throw new Exception("$fullName: upstreamWorkflowJob not found: '$upstreamProject'")
      }
      if (!(upstreamWorkflowJob instanceof org.jenkinsci.plugins.workflow.job.WorkflowJob)) {
        throw new Exception("$fullName: $upstreamProject: upstreamWorkflowJob is not a WorkflowJob: $upstreamWorkflowJob")
      }
      allUpstreamProjects.addAll(getAllUpstreamProjects(jenkins, upstreamProjectsCache, allUpstreamProjectsCache, upstreamWorkflowJob))
    }
    // Cache result
    allUpstreamProjectsCache[fullName] = allUpstreamProjects
  }
  return allUpstreamProjects
}

/*
 * Prunes upstream projects in two ways:
 *
 * 1) Remove duplicates by job fullName (handles different relative paths to same project)
 *
 * 2) Removes direct upstream projects that are also transitive through others
 */
def pruneUpstreamProjects(jenkins, upstreamProjectsCache, currentWorkflowJob, upstreamProjects) {
  // Quick unique by name, and ensures a new object to not affect parameter
  upstreamProjects = upstreamProjects.unique()
  // Find the set of all projects for each upstreamProject
  def upstreamFullNames = []
  def allUpstreamProjects = []
  def allUpstreamProjectsCache = [:]
  for (upstreamProject in upstreamProjects) {
    def upstreamWorkflowJob = jenkins.getItem(upstreamProject, currentWorkflowJob)
    if (upstreamWorkflowJob == null) {
      throw new Exception("${currentWorkflowJob.fullName}: upstreamWorkflowJob not found: '$upstreamProject'")
    }
    if (!(upstreamWorkflowJob instanceof org.jenkinsci.plugins.workflow.job.WorkflowJob)) {
      throw new Exception("${currentWorkflowJob.fullName}: $upstreamProject: upstreamWorkflowJob is not a WorkflowJob: $upstreamWorkflowJob")
    }
    upstreamFullNames << upstreamWorkflowJob.fullName
    allUpstreamProjects << getAllUpstreamProjects(jenkins, upstreamProjectsCache, allUpstreamProjectsCache, upstreamWorkflowJob)
  }
  allUpstreamProjectsCache = null
  // Prune upstream, quadratic algorithm
  def pruned = []
  def len = upstreamProjects.size()
  assert len == upstreamFullNames.size()
  assert len == allUpstreamProjects.size()
  for (int i = 0; i < len; i++) {
    def upstreamFullName = upstreamFullNames[i]
    // Make sure no contained by any other upstream
    def transitiveOnOther = false
    for (int j = 0; j < len; j++) {
      if (j != i && allUpstreamProjects[j].contains(upstreamFullName)) {
        //echo "pruneUpstreamProjects: ${currentWorkflowJob.fullName}: Pruned due to transitive: $upstreamFullName found in ${upstreamProjects[j]}"
        transitiveOnOther = true
        break
      }
    }
    if (!transitiveOnOther) {
      //echo "pruneUpstreamProjects: ${currentWorkflowJob.fullName}: Direct: ${upstreamProjects[i]}"
      pruned << upstreamProjects[i]
    }
  }
  return pruned
}

/*
 * Finds the depth of the given job in the upstream project graph
 * Top-most projects will be 1, which matches the job priorities for Priority Sorter plugin
 *
 * Uses depthMap for tracking
 *
 * Mapping from project full name to depth, where final depth is >= 1
 * During traversal, a project is first added to the map with a value of 0, which is used to detect graph loops
 */
def getDepth(jenkins, upstreamProjectsCache, depthMap, workflowJob, jobUpstreamProjects) {
  def fullName = workflowJob.fullName
  def depth = depthMap[fullName]
  if (depth == null) {
    // Add to map with value 0 for loop detection
    depthMap[fullName] = 0
    def maxUpstream = 0
    jobUpstreamProjects.each {upstreamProject ->
      def upstreamWorkflowJob = jenkins.getItem(upstreamProject, workflowJob)
      if (upstreamWorkflowJob == null) {
        throw new Exception("$fullName: upstreamWorkflowJob not found: '$upstreamProject'")
      }
      if (!(upstreamWorkflowJob instanceof org.jenkinsci.plugins.workflow.job.WorkflowJob)) {
        throw new Exception("$fullName: $upstreamProject: upstreamWorkflowJob is not a WorkflowJob: $upstreamWorkflowJob")
      }
      def upstreamProjects = getUpstreamProjects(jenkins, upstreamProjectsCache, upstreamWorkflowJob)
      def upstreamDepth = getDepth(jenkins, upstreamProjectsCache, depthMap, upstreamWorkflowJob, upstreamProjects)
      if (upstreamDepth > maxUpstream) maxUpstream = upstreamDepth
    }
    depth = maxUpstream + 1
    //echo "$fullName: $depth"
    depthMap[fullName] = depth
  } else if (depth == 0) {
    throw new Exception("$fullName: Loop detected in upstream project graph")
  }
  return depth;
}

// Variables temporarily used in project resolution
def tempUpstreamProjectsCache = [:]
def tempJenkins = Jenkins.get()
// Find the current project
def tempCurrentWorkflowJob = currentBuild.rawBuild.parent
if (!(tempCurrentWorkflowJob instanceof org.jenkinsci.plugins.workflow.job.WorkflowJob)) {
  throw new Exception("tempCurrentWorkflowJob is not a WorkflowJob: $tempCurrentWorkflowJob")
}

// Prune set of upstreamProjects
def prunedUpstreamProjects = pruneUpstreamProjects(tempJenkins, tempUpstreamProjectsCache, tempCurrentWorkflowJob, upstreamProjects)

if (!binding.hasVariable('buildPriority')) {
  // Find the longest path through all upstream projects, which will be used as both job priority and
  // nice value.  This will ensure proper build order in all cases.  However, it may prevent some
  // possible concurrency since reduction to simple job priority number loses information about which
  // are critical paths on the upstream project graph.
  def buildPriority = getDepth(tempJenkins, tempUpstreamProjectsCache, [:], tempCurrentWorkflowJob, prunedUpstreamProjects)
  if (buildPriority > 30) throw new Exception("buildPriority > 30, increase global configuration: $buildPriority")
  binding.setVariable('buildPriority', buildPriority)
}
if (buildPriority < 1 || buildPriority > 30) {
  throw new Exception("buildPriority out of range 1 - 30: $buildPriority")
}

// Remove temporary variables
tempUpstreamProjectsCache = null
tempJenkins = null
tempCurrentWorkflowJob = null

if (!binding.hasVariable('quietPeriod')) {
  binding.setVariable('quietPeriod', 10 + buildPriority * 2)
}
if (!binding.hasVariable('nice')) {
  def nice = buildPriority - 1;
  if (nice > 19) nice = 19;
  binding.setVariable('nice', nice)
}
if (!binding.hasVariable('maven')) {
  binding.setVariable('maven', 'maven-3')
}
if (!binding.hasVariable('mavenOpts')) {
  binding.setVariable('mavenOpts', '-Djansi.force')
}
if (!binding.hasVariable('mavenOptsJdk16')) {
  // See https://docs.openrewrite.org/getting-started/getting-started#running-on-jdk-16-and-newer
  binding.setVariable('mavenOptsJdk16', '--add-exports jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED --add-exports jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED --add-exports jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED --add-exports jdk.compiler/com.sun.tools.javac.jvm=ALL-UNNAMED --add-exports jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED --add-exports jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED --add-exports jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED --add-exports jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED --add-exports jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED')
}
if (!binding.hasVariable('extraProfiles')) {
  binding.setVariable('extraProfiles', [])
}
if (!binding.hasVariable('testWhenExpression')) {
  binding.setVariable('testWhenExpression',
    {return fileExists(projectDir + '/src/test')}
  )
}
if (!binding.hasVariable('sonarqubeWhenExpression')) {
  binding.setVariable('sonarqubeWhenExpression',
    {return !fileExists(projectDir + '/.github/workflows/build.yml')}
  )
}
if (!binding.hasVariable('failureEmailTo')) {
  binding.setVariable('failureEmailTo', 'support@aoindustries.com')
}

// Common settings
def mvnCommon   = "-Dstyle.color=always -DrequireLastBuild=${params.requireLastBuild} -N -U -Pjenkins,POST-SNAPSHOT${extraProfiles.isEmpty() ? '' : (',' + extraProfiles.join(','))}"
def buildPhases = 'clean process-test-classes'

// Determine nice command prefix or empty string for none
def niceCmd = (nice == 0) ? '' : "nice -n$nice "

//
// Scripts pulled out of pipeline due to "General error during class generation: Method too large"
//

// Make sure working tree not modified after checkout
def checkTreeUnmodifiedScriptCheckout(niceCmd) {
  return """#!/bin/bash
s="\$(${niceCmd}git status --short)"
if [ "\$s" != "" ]
then
  echo "Working tree modified after checkout:"
  echo "\$s"
  exit 1
fi
"""
}

// Make sure working tree not modified by build or test
def checkTreeUnmodifiedScriptBuild(niceCmd) {
  return """#!/bin/bash
s="\$(${niceCmd}git status --short)"
if [ "\$s" != "" ]
then
  echo "Working tree modified during build or test:"
  echo "\$s"
  exit 1
fi
"""
}

// Temporarily move surefire-reports before withMaven to avoid duplicate logging of test results
def moveSurefireReportsScript() {
  return """#!/bin/bash
if [ -d target/surefire-reports ]
then
  mv target/surefire-reports target/surefire-reports.do-not-report-twice
fi
"""
}

// Restore surefire-reports
def restoreSurefireReportsScript() {
  return """#!/bin/bash
if [ -d target/surefire-reports.do-not-report-twice ]
then
  mv target/surefire-reports.do-not-report-twice target/surefire-reports
fi
"""
}

// Make sure working tree not modified by deploy
def checkTreeUnmodifiedScriptDeploy(niceCmd) {
  return """#!/bin/bash
s="\$(${niceCmd}git status --short)"
if [ "\$s" != "" ]
then
  echo "Working tree modified during deploy:"
  echo "\$s"
  exit 1
fi
"""
}

pipeline {
  agent any
  options {
    ansiColor('xterm')
    disableConcurrentBuilds(abortPrevious: true)
    quietPeriod(quietPeriod)
    skipDefaultCheckout()
    timeout(time: 2, unit: 'HOURS')
    // Only allowed to copy build artifacts from self
    // See https://plugins.jenkins.io/copyartifact/
    copyArtifactPermission("/${JOB_NAME}")
    // See https://plugins.jenkins.io/build-history-manager/
    buildDiscarder(BuildHistoryManager([
      [
        // Keep most recent not_built build, which is useful to know which
        // builds have been superseded during their quiet period.
        conditions: [BuildResult(
          matchNotBuilt: true
        )],
        matchAtMost: 1,
        continueAfterMatch: false
      ],
      [
        // Keep most recent aborted build, which is useful to know what the build is waiting for
        // and to see that the build is still pending in Active and Blinkenlichten views.
        conditions: [BuildResult(
          matchAborted: true
        )],
        matchAtMost: 1,
        continueAfterMatch: false
      ],
      [
        // Keep most recent 50 success/unstable/failure builds
        conditions: [BuildResult(
          // All statuses except ABORTED from
          // https://github.com/jenkinsci/build-history-manager-plugin/blob/master/src/main/java/pl/damianszczepanik/jenkins/buildhistorymanager/model/conditions/BuildResultCondition.java
          matchSuccess: true,
          matchUnstable: true,
          matchFailure: true
        )],
        matchAtMost: 50,
        continueAfterMatch: false
      ],
      [
        actions: [DeleteBuild()]
      ]
    ]))
  }
  parameters {
    string(
      name: 'BuildPriority',
      defaultValue: "$buildPriority",
      description: """Specify the priority of this build.
Defaults to project's depth in the upstream project graph."""
    )
    booleanParam(
      name: 'requireLastBuild',
      defaultValue: true,
      description: """Is the last build required for the zip-timestamp-merge Ant task?
Defaults to true and will typically only be false for either the first build
or any build that adds or removes build artifacts."""
    )
  }
  triggers {
    upstream(
      threshold: hudson.model.Result.SUCCESS,
      upstreamProjects: "${prunedUpstreamProjects.join(', ')}"
    )
  }
  stages {
    stage('Check Ready') {
      when {
        expression {
          return abortOnUnreadyDependency
        }
      }
      steps {
        catchError(message: 'Aborted due to dependencies not ready', buildResult: 'ABORTED', stageResult: 'ABORTED') {
          script {
            // See https://javadoc.jenkins.io/jenkins/model/Jenkins.html
            // See https://javadoc.jenkins.io/hudson/model/Job.html
            // See https://javadoc.jenkins.io/hudson/model/Run.html
            // See https://javadoc.jenkins.io/hudson/model/Result.html
            def jenkins = Jenkins.get();
            // Get the mapping of all active dependencies and their current status
            def upstreamProjectsCache = [:]
            def allUpstreamProjectsCache = [:]
            // Find the current project
            def currentWorkflowJob = currentBuild.rawBuild.parent
            if (!(currentWorkflowJob instanceof org.jenkinsci.plugins.workflow.job.WorkflowJob)) {
              throw new Exception("currentWorkflowJob is not a WorkflowJob: ${currentWorkflowJob.fullName}")
            }
            // Get all upstream projects (and the current)
            def allUpstreamProjects = getAllUpstreamProjects(
              jenkins,
              upstreamProjectsCache,
              allUpstreamProjectsCache,
              currentWorkflowJob
            )
            // Remove current project
            if (!allUpstreamProjects.removeElement(currentWorkflowJob.fullName)) {
              throw new Exception("currentWorkflowJob is not in allUpstreamProjects: ${currentWorkflowJob.fullName}")
            }
            // Check queue and get statuses, stop searching on first found unready
            allUpstreamProjects.each {upstreamProject ->
              def upstreamWorkflowJob = jenkins.getItemByFullName(upstreamProject)
              if (upstreamWorkflowJob == null) {
                throw new Exception("${currentWorkflowJob.fullName}: upstreamWorkflowJob not found: '$upstreamProject'")
              }
              if (!(upstreamWorkflowJob instanceof org.jenkinsci.plugins.workflow.job.WorkflowJob)) {
                throw new Exception("${currentWorkflowJob.fullName}: $upstreamProject: upstreamWorkflowJob is not a WorkflowJob: $upstreamWorkflowJob")
              }
              def lastBuild = upstreamWorkflowJob.getLastBuild();
              if (lastBuild == null) {
                error("${currentWorkflowJob.fullName}: Aborting due to dependency never built: ${upstreamWorkflowJob.fullName}")
              }
              if (lastBuild.isBuilding()) {
                error("${currentWorkflowJob.fullName}: Aborting due to dependency currently building: ${upstreamWorkflowJob.fullName} #${lastBuild.number}")
              }
              def result = lastBuild.result;
              if (result != hudson.model.Result.SUCCESS) {
                error("${currentWorkflowJob.fullName}: Aborting due to dependency last build not successful: ${upstreamWorkflowJob.fullName} #${lastBuild.number} is $result")
              }
            }
          }
        }
      }
    }
    stage('Workaround Git #27287') {
      when {
        expression {
          return (
            currentBuild.result == null
            || currentBuild.result == hudson.model.Result.SUCCESS
            || currentBuild.result == hudson.model.Result.UNSTABLE
          ) && projectDir != '.' && fileExists('.gitmodules')
        }
      }
      steps {
        // See https://www.jenkins.io/doc/pipeline/steps/workflow-scm-step/#checkout-check-out-from-version-control
        // See https://stackoverflow.com/questions/43293334/sparsecheckout-in-jenkinsfile-pipeline
        /*
         * Git version 2.34.1 is failing when fetching without submodules, which is our most common usage.
         * It fails only on the first fetch, then succeeds on subsequent fetches.
         * This issue is expected to be resolved in 2.35.1.
         *
         * To workaround this issue, we are allowing to retry the Git fetch by catching the first failure.
         *
         * See https://github.com/git/git/commit/c977ff440735e2ddc2ef18b18ae9a653bb8650fe
         * See https://gitlab.com/gitlab-org/gitlab/-/issues/27287
         *
         * TODO: Remove once on Git >= 2.35.1
         */
        catchError(message: 'Git 2.34.1 first fetch fails when not fetching submodules, will retry', buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
          checkout scm: [$class: 'GitSCM',
            userRemoteConfigs: [[
              url: scmUrl,
              refspec: "+refs/heads/$scmBranch:refs/remotes/origin/$scmBranch"
            ]],
            branches: [[name: "refs/heads/$scmBranch"]],
            browser: scmBrowser,
            extensions: [
              // CleanCheckout was too aggressive and removed the workspace .m2 folder, added "sh" steps below
              // [$class: 'CleanCheckout'],
              [$class: 'CloneOption',
                // See https://issues.jenkins.io/browse/JENKINS-45586
                shallow: true,
                depth: 20,
                honorRefspec: true
              ],
              [$class: 'SparseCheckoutPaths',
                sparseCheckoutPaths: sparseCheckoutPaths
              ],
              [$class: 'SubmoduleOption',
                disableSubmodules: disableSubmodules,
                shallow: true,
                depth: 20
              ]
            ]
          ]
        }
      }
    }
    stage('Checkout SCM') {
      when {
        expression {
          return (
            currentBuild.result == null
            || currentBuild.result == hudson.model.Result.SUCCESS
            || currentBuild.result == hudson.model.Result.UNSTABLE
          )
        }
      }
      steps {
        checkout scm: [$class: 'GitSCM',
          userRemoteConfigs: [[
            url: scmUrl,
            refspec: "+refs/heads/$scmBranch:refs/remotes/origin/$scmBranch"
          ]],
          branches: [[name: "refs/heads/$scmBranch"]],
          browser: scmBrowser,
          extensions: [
            // CleanCheckout was too aggressive and removed the workspace .m2 folder, added "sh" steps below
            // [$class: 'CleanCheckout'],
            [$class: 'CloneOption',
              // See https://issues.jenkins.io/browse/JENKINS-45586
              shallow: true,
              depth: 20,
              honorRefspec: true
            ],
            [$class: 'SparseCheckoutPaths',
              sparseCheckoutPaths: sparseCheckoutPaths
            ],
            [$class: 'SubmoduleOption',
              disableSubmodules: disableSubmodules,
              shallow: true,
              depth: 20
            ]
          ]
        ]
        sh "${niceCmd}git verify-commit HEAD"
        sh "${niceCmd}git reset --hard"
        // git clean -fdx was iterating all of /.m2 despite being ignored
        sh "${niceCmd}git clean -fx -e ${(projectDir == '.') ? '/.m2' : ('/' + projectDir + '/.m2')}"
        // Make sure working tree not modified after checkout
        sh checkTreeUnmodifiedScriptCheckout(niceCmd)
      }
    }
    stage('Builds') {
      matrix {
        when {
          expression {
            return (
              currentBuild.result == null
              || currentBuild.result == hudson.model.Result.SUCCESS
              || currentBuild.result == hudson.model.Result.UNSTABLE
            )
          }
        }
        axes {
          axis {
            name 'jdk'
            values '11', '17', '20' // buildJdks
          }
        }
        stages {
          stage('Build') {
            steps {
              dir(projectDir) {
                withMaven(
                  maven: maven,
                  mavenOpts: "${jdk == '11' ? mavenOpts : "$mavenOpts $mavenOptsJdk16"}",
                  mavenLocalRepo: ".m2/repository-jdk-$jdk",
                  jdk: "jdk-$jdk"
                ) {
                  sh "${niceCmd}$MVN_CMD $mvnCommon ${jdk == deployJdk ? '' : "-Dalt.build.dir=target-jdk-$jdk "}$buildPhases"
                }
              }
              script {
                // Create a separate copy for full test matrix
                if (testWhenExpression.call()) {
                  testJdks.each() {testJdk ->
                    if (testJdk != jdk) {
                      sh "${niceCmd}rm $projectDir/target-jdk-$jdk-$testJdk -rf"
                      sh "${niceCmd}cp -al $projectDir/target${jdk == deployJdk ? '' : "-jdk-$jdk"} $projectDir/target-jdk-$jdk-$testJdk"
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    stage('Tests') {
      matrix {
        when {
          expression {
            return (
              currentBuild.result == null
              || currentBuild.result == hudson.model.Result.SUCCESS
              || currentBuild.result == hudson.model.Result.UNSTABLE
            ) && testWhenExpression.call()
          }
        }
        axes {
          axis {
            name 'jdk'
            values '11', '17', '20' // buildJdks
          }
          axis {
            name 'testJdk'
            values '11', '17', '20' // testJdks
          }
        }
        stages {
          stage('Test') {
            environment {
              buildDir  = "target${(testJdk == jdk) ? (jdk == deployJdk ? '' : "-jdk-$jdk") : ("-jdk-$jdk-$testJdk")}"
              coverage  = "${(jdk == deployJdk && testJdk == deployJdk && fileExists(projectDir + '/src/main/java') && fileExists(projectDir + '/src/test')) ? '-Pcoverage' : '-P!coverage'}"
              testGoals = "${(coverage == '-Pcoverage') ? 'jacoco:prepare-agent surefire:test jacoco:report' : 'surefire:test'}"
            }
            steps {
              dir(projectDir) {
                withMaven(
                  maven: maven,
                  mavenOpts: "${testJdk == '11' ? mavenOpts : "$mavenOpts $mavenOptsJdk16"}",
                  mavenLocalRepo: ".m2/repository-jdk-$jdk",
                  jdk: "jdk-$testJdk"
                ) {
                  sh "${niceCmd}$MVN_CMD $mvnCommon -Dalt.build.dir=$buildDir $coverage $testGoals"
                }
              }
            }
          }
        }
      }
    }
    stage('Deploy') {
      when {
        expression {
          return (
            currentBuild.result == null
            || currentBuild.result == hudson.model.Result.SUCCESS
            || currentBuild.result == hudson.model.Result.UNSTABLE
          )
        }
      }
      steps {
        // Steps moved to separate function to avoid "Method too large"
        // See https://stackoverflow.com/a/47631522
        deploySteps(niceCmd, projectDir, deployJdk, maven, mavenOpts, mavenOptsJdk16, mvnCommon)
      }
    }
    stage('SonarQube analysis') {
      when {
        expression {
          return (
            currentBuild.result == null
            || currentBuild.result == hudson.model.Result.SUCCESS
            || currentBuild.result == hudson.model.Result.UNSTABLE
          ) && sonarqubeWhenExpression.call()
        }
      }
      steps {
        // Steps moved to separate function to avoid "Method too large"
        // See https://stackoverflow.com/a/47631522
        sonarQubeAnalysisSteps(niceCmd, projectDir, deployJdk, maven, mavenOpts, mavenOptsJdk16, mvnCommon)
      }
    }
    stage('Quality Gate') {
      when {
        expression {
          return (
            currentBuild.result == null
            || currentBuild.result == hudson.model.Result.SUCCESS
            || currentBuild.result == hudson.model.Result.UNSTABLE
          ) && sonarqubeWhenExpression.call()
        }
      }
      steps {
        // Steps moved to separate function to avoid "Method too large"
        // See https://stackoverflow.com/a/47631522
        qualityGateSteps()
      }
    }
    stage('Analysis') {
      when {
        expression {
          return (
            currentBuild.result == null
            || currentBuild.result == hudson.model.Result.SUCCESS
            || currentBuild.result == hudson.model.Result.UNSTABLE
          )
        }
      }
      steps {
        // Steps moved to separate function to avoid "Method too large"
        // See https://stackoverflow.com/a/47631522
        analysisSteps()
      }
    }
  }
  post {
    failure {
      emailext to: failureEmailTo,
        subject: "[Jenkins] ${currentBuild.fullDisplayName} build failed",
        body: "${env.BUILD_URL}console"
    }
  }
}

// Steps moved to separate function to avoid "Method too large"
// See https://stackoverflow.com/a/47631522
void deploySteps(niceCmd, projectDir, deployJdk, maven, mavenOpts, mavenOptsJdk16, mvnCommon) {
  // Make sure working tree not modified by build or test
  sh checkTreeUnmodifiedScriptBuild(niceCmd)
  dir(projectDir) {
    // Download artifacts from last successful build of this job
    // See https://plugins.jenkins.io/copyartifact/
    // See https://www.jenkins.io/doc/pipeline/steps/copyartifact/#copyartifacts-copy-artifacts-from-another-project
    copyArtifacts(
      projectName: "/${JOB_NAME}",
      selector: lastSuccessful(stable: true),
      // *.pom included so pom-only projects have something to successfully download
      // The other extensions match the types processed by ao-ant-tasks
      filter: '**/*.pom, **/*.aar, **/*.jar, **/*.war, **/*.zip',
      target: 'target/last-successful-artifacts',
      flatten: true,
      optional: !params.requireLastBuild
    )
    // Temporarily move surefire-reports before withMaven to avoid duplicate logging of test results
    sh moveSurefireReportsScript()
    withMaven(
      maven: maven,
      mavenOpts: "${deployJdk == '11' ? mavenOpts : "$mavenOpts $mavenOptsJdk16"}",
      mavenLocalRepo: ".m2/repository-jdk-$deployJdk",
      jdk: "jdk-$deployJdk"
    ) {
      sh "${niceCmd}$MVN_CMD $mvnCommon -Pnexus,jenkins-deploy,publish deploy"
    }
    // Restore surefire-reports
    sh restoreSurefireReportsScript()
  }
  // Make sure working tree not modified by deploy
  sh checkTreeUnmodifiedScriptDeploy(niceCmd)
}

// Steps moved to separate function to avoid "Method too large"
// See https://stackoverflow.com/a/47631522
void sonarQubeAnalysisSteps(niceCmd, projectDir, deployJdk, maven, mavenOpts, mavenOptsJdk16, mvnCommon) {
  sh "${niceCmd}git fetch --unshallow || true" // SonarQube does not currently support shallow fetch
  dir(projectDir) {
    withSonarQubeEnv(installationName: 'AO SonarQube') {
      withMaven(
        maven: maven,
        mavenOpts: "${deployJdk == '11' ? mavenOpts : "$mavenOpts $mavenOptsJdk16"}",
        mavenLocalRepo: ".m2/repository-jdk-$deployJdk",
        jdk: "jdk-$deployJdk"
      ) {
        sh "${niceCmd}$MVN_CMD $mvnCommon -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml sonar:sonar"
      }
    }
  }
}

// Steps moved to separate function to avoid "Method too large"
// See https://stackoverflow.com/a/47631522
void qualityGateSteps() {
  timeout(time: 1, unit: 'HOURS') {
    waitForQualityGate(webhookSecretId: 'SONAR_WEBHOOK', abortPipeline: false)
  }
}

// Steps moved to separate function to avoid "Method too large"
// See https://stackoverflow.com/a/47631522
void analysisSteps() {
  recordIssues(
    aggregatingResults: true,
    skipPublishingChecks: true,
    sourceCodeEncoding: 'UTF-8',
    tools: [
      checkStyle(),
      java(),
      javaDoc(),
      // junitParser(),
      mavenConsole(),
      // php()
      sonarQube(),
      spotBugs()
      // taskScanner()
    ]
  )
}
