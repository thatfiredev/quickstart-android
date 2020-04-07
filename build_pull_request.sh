#!/bin/bash

# Exit on error
set -e

# Copy mock google-services file
echo "Using mock google-services.json"
cp mock-google-services.json admob/app/google-services.json
cp mock-google-services.json analytics/app/google-services.json
cp mock-google-services.json app-indexing/app/google-services.json
cp mock-google-services.json auth/app/google-services.json
cp mock-google-services.json config/app/google-services.json
cp mock-google-services.json crash/app/google-services.json
cp mock-google-services.json database/app/google-services.json
cp mock-google-services.json dynamiclinks/app/google-services.json
cp mock-google-services.json firestore/app/google-services.json
cp mock-google-services.json functions/app/google-services.json
cp mock-google-services.json inappmessaging/app/google-services.json
cp mock-google-services.json perf/app/google-services.json
cp mock-google-services.json messaging/app/google-services.json
cp mock-google-services.json mlkit/app/google-services.json
cp mock-google-services.json mlkit-langid/app/google-services.json
cp mock-google-services.json mlkit-smartreply/app/google-services.json
cp mock-google-services.json mlkit-translate/app/google-services.json
cp mock-google-services.json storage/app/google-services.json


AVAILABLE_TASKS=$(./gradlew tasks --all)

echo "HEAD ENV ${GITHUB_HEAD_REF}"
echo "BASE ENV ${GITHUB_BASE_REF}"

# unshallow since GitHub actions does a shallow clone
git fetch --unshallow
git fetch origin

# Check which modules were changed
echo "git remote:"
git remote

echo "git branch:"
git branch

echo "Repo: ${GITHUB_REPOSITORY}"

git diff --name-only origin/$GITHUB_BASE_REF..origin/$GITHUB_HEAD_REF | { while read line
  changed_modules=""
  do
    module_name=${line%%/*}
    if [[ ${changed_modules} != *"${module_name}"* ]]; then
      changed_modules="${changed_modules} ${module_name}"
      echo "adding ${module_name}"
    fi
  done
  export MODULES=$changed_modules
}

changed_modules=$MODULES

echo "Modules:${changed_modules}"
echo $changed_modules
# Check if these modules have gradle tasks
build_commands=""
for module in $changed_modules
do
  echo "Changed module:"
  echo $module
  if [[ $AVAILABLE_TASKS =~ $module":app:" ]]; then
    build_commands=${build_commands} " :"${module}":app:assembleDebug :"${module}":app:check"
    echo "Building debug for ${module}"
  fi
done

#if [[ $build_commands == "" ]]; then
#  # The changes were made in directories with no gradle tasks
#  # Let's build debug, just in case
#  build_commands=" assembleDebug check"
#  echo "No gradle tasks were found. Building debug..."
#fi

# Build
#echo "Building Pull Request..."
#eval "./gradlew clean ktlint ${build_commands}"
