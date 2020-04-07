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
changed_modules=""

# Check which modules were changed
git diff --name-only | { while read line
  do
    module_name=${line%%/*}
    if [[ ${changed_modules} != *"$module_name"* ]]; then
      changed_modules="${changed_modules} ${module_name}"
    fi
  done
}

# Check if these modules have gradle tasks
build_commands=""
for module in $changed_modules
do
  if [[ $AVAILABLE_TASKS =~ $module":app:" ]]; then
    build_commands="${build_commands} :${module}:app:assembleDebug :${module}:app:check"
  fi
done

if [[ $build_commands == "" ]]; then
  # The changes were made in directories with no gradle tasks
  # Let's build debug, just in case
  build_commands=" assembleDebug check"
fi

# Build
echo "Building Pull Request..."
# On a pull request, just build debug which is much faster and catches
# obvious errors.
eval "./gradlew clean ktlint ${build_commands}"
