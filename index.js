const shell = require('shelljs');
const core = require('@actions/core');
const github = require('@actions/github');

let forAllModules = core.getInput('for-all-modules');
let forEachModule = core.getInput('for-each-module');

// unshallow since GitHub actions does a shallow clone
shell.exec('git fetch --unshallow');
shell.exec('git fetch origin');

// Get all changed modules

const baseBranch = 'origin/' + process.env.GITHUB_BASE_REF;
shell.exec('git diff --name-only ' + baseBranch, function (code, stdout, stderr) {
    console.log('Exit code:', code);
    console.log('Std Out:', stdout);
    console.log('Std Error:', stderr);
});
