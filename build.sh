#!/bin/bash

# Exit on error
set -e

# Work off travis
if [[ ! -z TRAVIS_PULL_REQUEST ]]; then
  echo "TRAVIS_PULL_REQUEST: $TRAVIS_PULL_REQUEST"
else
  echo "TRAVIS_PULL_REQUEST: unset, setting to false"
  TRAVIS_PULL_REQUEST=false
fi

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

# Install preview deps
${ANDROID_HOME}/tools/bin/sdkmanager --channel=3 \
  "tools" "platform-tools" "build-tools;26.0.0-rc2" "platforms;android-26"

# Build
if [ $TRAVIS_PULL_REQUEST = false ] ; then
  echo "Building full project"
  # For a merged commit, build all configurations.
  ./gradlew clean ktlint build
else
  # On a pull request, just build debug which is much faster and catches
  # obvious errors.

  dest="$TRAVIS_BRANCH"
  branch="HEAD"

  # TODO: Delete the lines below once we're done
  echo "destination= $dest"
  echo "origin= $branch"

  # The build commands to execute
  build_commands="./gradlew ktlint"

  # Look for available tasks
  echo "Looking for available tasks"
  AVAILABLE_TASKS=$(./gradlew tasks --all)

  echo "Running git branch"
  git branch

  echo "Running git diff"
  git diff --name-only $dest..$branch -- | { while read line
      do
        module_name=${line%%/*}
        # echo "current module name: $module_name"

        if [[ $AVAILABLE_TASKS =~ "${module_name}:" && ${build_commands} != *"$module_name"* ]]; then
            echo "adding command for ${module_name}:"
            build_commands="${build_commands} ${module_name}:app:clean"
            build_commands="${build_commands} ${module_name}:app:assembleDebug"
            build_commands="${build_commands} ${module_name}:app:check"
        fi
      done
      echo "build_commands: ${build_commands}"
      eval $build_commands
  }

fi
