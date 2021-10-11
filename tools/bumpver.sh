#!/usr/bin/env bash

#*********************************************************
# globals
#*********************************************************

# filepaths are relative to the repo root
APP_GRADLE_FILEPATH="./app/build.gradle"

#*********************************************************
# functions
#*********************************************************

function displayHelpMessage() {
  echo "
Tool for bumping the project version. Call this from the repo root as ./tools/bumpver.sh.

Bumpver will create a commit of its changes. If there are any existing staged or unstaged changes in
the working tree bumpver will cancel without changing anything. If the option --stage is used,
bumpver will work with those existing changes.

Usages:
bumpver
bumpver major|minor|fix|beta|pre-release|release [options]

<no args>
  Using bumpver with no args simply displays the current version.

major|minor|fix|beta|pre-release|release
  Use one of these commands to increment the related version number.
  Less significant version numbers are reset as needed.
  Examples:
    major: 1.2-beta.3 -> 2.0-alpha.1
    minor: 1.2-beta.3 -> 1.3-alpha.1
    fix: 1.2.3 -> 1.2.4
    beta: 1.2-alpha.3 -> 1.2-beta.1
    pre-release: 1.2-alpha.3 -> 1.2-alpha.4
                 1.2-beta.3 -> 1.2-beta.4
    release: 1.2-beta.3 -> 1.2.0

  These commands are restricted by which version types they are allowed to bump from:
    major: bumps from beta
    minor: bumps from beta
    fix: bumps from release
    beta: bumps from alpha
    pre-release: bumps from alpha, beta
    release: bumps from beta
  This script will show an error if you try to bump a version with an incompatible command.

  These commands also bump the version code as well. The version code bumps are defined as 'large'
  or 'small' bumps. Large bumps relate to the upper 4 code digits, while small bumps relate to the
  lower 5. Each version name bump has an associated type of version code bump:
    Large: major, minor, beta, release
    Small: fix, pre-release
  Examples:
    large: 100123 -> 200001, 123 -> 100001
    small: 100123 -> 100124, 123 -> 124

Options:
-h --help         Display this help message.
-s --stage        By default bumpver creates a new tagged commit of its changes. Use this option to
                  instead only stage those changes without committing.
-p --no-prompt    Do not show a prompt before changing the version.
"

}

function displayError() {
  echo "Error: $*" >&2
}

function getCurrentVersionName() {
  # find versionName line; print only the contents between the quotes
  grep "versionName" "$APP_GRADLE_FILEPATH" | sed -r 's/.*\"(.*)\".*/\1/'
}

function getCurrentVersionCode() {
  # find versionCode line; remove everthing but the code value
  grep "versionCode" "$APP_GRADLE_FILEPATH" | sed -r 's/.*versionCode //'
}

function isWorkingTreeDirty() {
  if [[ -n "$(git status --porcelain)" ]]; then
    return 0
  else
    return 1
  fi
}

# Check if a value is contained in an array.
# Args:
#   value: the value to find
#   *: the remaining args are the array values to search
# Returns:
#   0 if found, 1 if not found.
function doesArrayContainValue() {
  local value="$1"; shift
  for other_value in "$@"; do
    if [[ "$value" == "$other_value" ]]; then
      return 0
    fi
  done
  return 1
}

function getVersionTypeOf() {
  local versionName="$1"
  if echo "$versionName" | grep -E -q '^[0-9]+\.[0-9]+\.[0-9]+$'; then
    echo "release"
  elif echo "$versionName" | grep -E -q '^[0-9]+\.[0-9]+-beta\.[0-9]+$'; then
    echo "beta"
  elif echo "$versionName" | grep -E -q '^[0-9]+\.[0-9]+-alpha\.[0-9]+$'; then
    echo "alpha"
  else
    echo "unknown"
  fi
}

function getUserConfirmation() {
    echo "Current version $1 will be bumped to $2. Proceed?"
    # https://stackoverflow.com/a/226724
    select selection in "Proceed" "Abort"; do
        case $selection in
            Proceed )
                return 0
            ;;
            Abort )
                return 1
            ;;
        esac
    done
}

# Args:
#   command: the user-provided bump command (used for error messages)
#   bumpFunction: the version-bumping function to run. This should take a version name & code as
#     args, and should set its return value to the variable 'bumpFunctionResult'
#   shouldPrompt: should the user be prompted before bumping the version?
#   *: remaining args are the compatible version types for this command.
# Returns:
#   The exit code of bumpFunction.
function bumpVersion() {
  local command="$1"; shift
  local bumpFunction="$1"; shift
  local shouldPrompt="$1"; shift
  local compatibleTypes=( "$@" )

  local versionName
  local versionCode
  local versionType
  versionName="$( getCurrentVersionName )"
  versionCode="$( getCurrentVersionCode )"
  versionType="$( getVersionTypeOf "$versionName" )"

  if [[ "$versionType" == "unknown" ]]; then
    displayError "Unknown version syntax: ${versionName}"
    return 1
  fi

  if ! doesArrayContainValue "$versionType" "${compatibleTypes[@]}"; then
    displayError "Command '${command}' is not compatible with the version ${versionName}"
    return 1
  fi

  local bumpFunctionResult
  local newName
  local newCode
  # version name syntax is validated in getVersionTypeOf, so bumpFunction doesn't need to
  # check again
  "$bumpFunction" "$versionName" "$versionCode"
  newName="${bumpFunctionResult[0]}"
  newCode="${bumpFunctionResult[1]}"

  if [[ "$shouldPrompt" == "true" ]]; then
    if ! getUserConfirmation "$versionName" "$newName"; then
      echo "Aborting version bump operation."
      return 1
    fi
  fi

  updateVersionInProject "$newName" "$newCode"
}

# Bump the provided version code.
# Args:
#   code: the code to bump
#   bumpType: can be 'large' or 'small'. This relates to which digit-group to bump - the larger or
#     smaller digits
#     E.g.:
#       123 -> 'large' -> 100001
#       100123 -> 'large' -> 200001
#       123 -> 'small' -> 124
#       100123 -> 'small' -> 100124.
#   bumpAmount: the amount to bump the code. (This is needed in certain situations, like
#     branching for release, where you need to bump 2 for the next release alpha to skip over the
#     release)
function bumpCode() {
  local code="$1"
  local bumpType="$2"
  local bumpAmount="$3"

  if [[ "$bumpType" == 'large' ]]; then
    echo "$(( ( ( (code/100000) + bumpAmount ) * 100000 ) + 1 ))"
  elif [[ "$bumpType" == 'small' ]]; then
    echo "$(( code + bumpAmount ))"
  else
    echo "$code"
  fi
}

function bumpMajor() {
  local name="$1"
  local code="$2"

  # bump name
  local majorNum
  majorNum="$( echo "$name" | sed -r 's/^([0-9]+).*/\1/' )"
  majorNum=$(( majorNum + 1 ))
  local newName
  newName="${majorNum}.0-alpha.1"

  # bump code
  local newCode
  newCode="$( bumpCode "$code" "large" 2 )"

  bumpFunctionResult=( "$newName" "$newCode" )
}

function bumpMinor() {
  local name="$1"
  local code="$2"

  # bump name
  local minorNum
  minorNum="$( echo "$name" | sed -r 's/^[0-9]+\.([0-9]+).*/\1/' )"
  minorNum=$(( minorNum + 1 ))
  local newName
  newName="$( echo "$name" | sed -r "s/^([0-9]+).*/\\1\\.${minorNum}-alpha\\.1/" )"

  # bump code
  local newCode
  newCode="$( bumpCode "$code" "large" 2 )"

  bumpFunctionResult=( "$newName" "$newCode" )
}

function bumpFix() {
  local name="$1"
  local code="$2"

  # bump name
  local fixNum
  fixNum="$( echo "$name" | sed -r 's/^[0-9]+\.[0-9]+\.(.*)/\1/' )"
  fixNum=$(( fixNum + 1 ))
  local newName
  newName="$( echo "$name" | sed -r "s/^([0-9]+\\.[0-9]+\\.).*/\\1${fixNum}/" )"

  # bump code
  local newCode
  newCode="$( bumpCode "$code" "small" 1 )"

  bumpFunctionResult=( "$newName" "$newCode" )
}

function bumpBeta() {
  local name="$1"
  local code="$2"

  # bump name
  local newName
  newName="$( echo "$name" | sed -r 's/^(.*)alpha\.[0-9]+(.*)/\1beta.1\2/' )"

  # bump code
  local newCode
  newCode="$( bumpCode "$code" "large" 1 )"

  bumpFunctionResult=( "$newName" "$newCode" )
}

function bumpPreRelease() {
  local name="$1"
  local code="$2"

  # bump name
  local prereleaseNum
  prereleaseNum="$( echo "$name" | sed -r 's/^.*-.*\.(.*)/\1/' )"
  prereleaseNum=$(( prereleaseNum + 1 ))
  local newName
  newName="$( echo "$name" | sed -r "s/^(.*-.*\\.).*/\\1${prereleaseNum}/" )"

  # bump code
  local newCode
  newCode="$( bumpCode "$code" "small" 1 )"

  bumpFunctionResult=( "$newName" "$newCode" )
}

function bumpRelease() {
  local name="$1"
  local code="$2"

  # bump name
  local newName
  newName="$( echo "$name" | sed -r 's/^([0-9]+\.[0-9]+).*/\1\.0/' )"

  # bump code
  local newCode
  newCode="$( bumpCode "$code" "large" 1 )"

  bumpFunctionResult=( "$newName" "$newCode" )
}

# Actually update the project files
function updateVersionInProject() {
  local newName="$1"
  local newCode="$2"

  sed -r \
      -e "s/^(.*versionName).*/\\1 \\\"${newName}\\\"/" \
      -e "s/^(.*versionCode).*/\\1 ${newCode}/" \
      "$APP_GRADLE_FILEPATH" \
      > tmp && mv tmp "$APP_GRADLE_FILEPATH"
}

#*********************************************************
# main
#*********************************************************

function main() {
  set -e -u -o pipefail

  # if no args, just display the current version and exit
  # -------------------------------------------------------------
  if (( $# == 0 )); then
    getCurrentVersionName
    getCurrentVersionCode
    exit 0
  fi


  # parse args
  # -------------------------------------------------------------
  local optionStage="false"
  local optionUsePrompt="true"
  local positionalArgs=()

  for arg in "$@"; do
    case "$arg" in
      -h|--help)
        displayHelpMessage
        exit 0
      ;;
      -s|--stage)
        optionStage="true"; shift
      ;;
      -p|--no-prompt)
        optionUsePrompt="false"; shift
      ;;
      *)
        positionalArgs+=("$arg"); shift
      ;;
    esac
  done

  if (( ${#positionalArgs[@]} != 1 )); then
    displayError "Too many positional arguments (expected 1)"
    exit 1
  fi

  if isWorkingTreeDirty; then
    if [[ "$optionStage" == "false" ]]; then
      displayError "There are uncommitted changes in the working tree. Please commit or reset, then try again."
      exit 1
    fi
  fi

  bumpCommand="${positionalArgs[0]}"

  # bump the version number
  # -------------------------------------------------------------
  case "$bumpCommand" in
    major)
      bumpVersion "$bumpCommand" "bumpMajor" "$optionUsePrompt" "beta"
    ;;
    minor)
      bumpVersion "$bumpCommand" "bumpMinor" "$optionUsePrompt" "beta"
    ;;
    fix)
      bumpVersion "$bumpCommand" "bumpFix" "$optionUsePrompt" "release"
    ;;
    beta)
      bumpVersion "$bumpCommand" "bumpBeta" "$optionUsePrompt" "alpha"
    ;;
    pre-release)
      bumpVersion "$bumpCommand" "bumpPreRelease" "$optionUsePrompt" "alpha" "beta"
    ;;
    release)
      bumpVersion "$bumpCommand" "bumpRelease" "$optionUsePrompt" "beta"
    ;;
    *)
      displayError "Unrecognized command '${bumpCommand}'"
      exit 1
    ;;
  esac


  # commit changes
  # -------------------------------------------------------------
  versionName="$( getCurrentVersionName )"

  if [[ "$optionStage" == 'false' ]]; then
    git add --all
    git commit -m "version: bump version to ${versionName}" --quiet --no-verify
    git tag -a "v${versionName}" -m ""
    echo
    echo "Committed changes."
  else
    echo "Changes are staged for committing."
  fi

  echo "Bumped version to ${versionName}"
}

#*********************************************************
# script
#*********************************************************

main "$@"