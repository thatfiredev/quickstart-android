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

echo "Repo: ${GITHUB_REPOSITORY}"

while read line; do
  module_name=${line%%/*}
  if [[ ${MODULES} != *"${module_name}"* ]]; then
    MODULES="${MODULES} ${module_name}"
  fi
done < <(git diff --name-only origin/$GITHUB_BASE_REF..origin/$GITHUB_HEAD_REF)

# Check if these modules have gradle tasks
changed_modules=$MODULES
build_commands=""
for module in $changed_modules
do
  if [[ $AVAILABLE_TASKS =~ $module":app:" ]]; then
    build_commands=${build_commands}" :"${module}":app:assembleDebug :"${module}":app:check"
  fi
done

# Build
echo "Building Pull Request with"
echo $build_commands
eval "./gradlew clean ktlint ${build_commands}"
