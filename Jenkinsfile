def GCs   = ["none","boehm", "immix"]
def OSs   = ["mac", "linux"]
def tasks = [:]

def setBuildStatus(String message, String state, String ctx, String repoUrl, String commitSha) {
    step([
        $class: "GitHubCommitStatusSetter",
        reposSource: [$class: "ManuallyEnteredRepositorySource", url: repoUrl],
        commitShaSource: [$class: "ManuallyEnteredShaSource", sha: commitSha],
        contextSource: [$class: "ManuallyEnteredCommitContextSource", context: ctx],
        errorHandlers: [[$class: "ChangingBuildStatusErrorHandler", result: "UNSTABLE"]],
        statusResultSource: [ $class: "ConditionalStatusResultSource", results: [[$class: "AnyBuildResult", message: message, state: state]] ]
    ]);
}

for(int i = 0; i < OSs.size(); i++) {
    def selectedOS = OSs[i]
    tasks["${selectedOS}"] = {
        job(selectedOS, GCs)
    }
}

def job(String OS, List<String> GCs) {
    def repoUrl   = ""
    def commitSha = ""

    def advance = { name, ctx, work ->
        stage("[$ctx] $name") {
            ansiColor('xterm') {
                setBuildStatus("$name...", "PENDING", ctx, repoUrl, commitSha)
                try {
                    work()
                    setBuildStatus(name, "SUCCESS", ctx, repoUrl, commitSha)
                }
                catch (exc) {
                    setBuildStatus(name, "FAILURE", ctx, repoUrl, commitSha)
                    throw exc
                }
            }
        }
    }

    return node(OS) {
        stage("[$OS] Cloning") {
            ansiColor('xterm') {
                checkout scm

                sh "git config --get remote.origin.url > .git/remote-url"
                repoUrl = readFile(".git/remote-url").trim()

                sh "git rev-parse HEAD > .git/current-commit"
                commitSha = readFile(".git/current-commit").trim()
            }
        }

        advance("Formatting", OS) {
            sh 'bin/scalafmt --test'
        }

        advance("Building", OS) {
            retry(2) {
                sh "sbt -no-share -J-Xmx3G rebuild"
            }
        }

        setBuildStatus("Build succeeded", "SUCCESS", OS, repoUrl, commitSha)

        for (int i = 0; i < GCs.size(); i++) {
            def GC = GCs[i]
            advance("Testing", "$OS/$GC") {
                retry(2) {
                    sh "SCALANATIVE_GC=$GC sbt -no-share -J-Xmx3G test-all"
                }
            }
            setBuildStatus("Tests succeeded", "SUCCESS", "$OS/$GC", repoUrl, commitSha)
        }

    }
}

parallel tasks
