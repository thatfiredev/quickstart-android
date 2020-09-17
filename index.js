const shell = require('shelljs')
const core = require('@actions/core');

let forAllModules = core.getInput('for-all-modules');
let forEachModule = core.getInput('for-each-module');

shell.exec('./entrypoint.sh ' + forEachModule + ' ' + forAllModules)
