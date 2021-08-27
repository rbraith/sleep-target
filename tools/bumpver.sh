#!/bin/sh
#
# Use this script to bump the version.

# TODO [21-08-27 5:11PM] -- I've changed the versioning strat for this app and need to update this
#  tool to reflect that.
#  New versioning strat:
#   - beta versions happen on main (instead of the release branch)
#     - This change is to fix the situation where after bumping for release, for a time the next
#       alpha version and the current beta will be effectively the same version, due to bugfixes
#       flowing up
#  - no more hotfix betas e.g. 1.0.4-beta.3
#     - each release only has 1 beta stage
#  - new version code strat:
#     - its not nec. to reflect detail of the version name in the code - the code's only
#       responsibility is to *sort versions*, nothing else needs to be encoded.
#     - instead of MmmHHTBB use MMMMmmmmm, where M="major bump" and m="minor bump"
#       - Note: major & minor bumps here are not the major & minor version nums, these bumps work
#         like this:
#           - major: alpha->beta, beta->release, beta->next alpha
#           - minor: alpha nums, beta nums, release fix num
#       - examples:
#         1.0-pre-alpha -> 1.0-alpha.1 : 1 -> 100001
#         1.0-alpha.1 -> 1.0-alpha.2 : 100001 -> 100002
#         1.0-alpha.2 -> 1.0-beta.1 : 100002 -> 200001
#         1.0-beta.1 -> 1.0-beta.2 : 200001 -> 200002
#         1.0-beta.2 -> 1.0.0 : 200002 -> 300001
#         1.0-beta.2 -> 1.1-alpha.1 : 200002 -> 400001.

#*********************************************************
# globals
#*********************************************************

# filepaths are relative to the repo root
APP_GRADLE_FILEPATH="./app/build.gradle"

TYPE_NUM_ALPHA="1"
TYPE_NUM_BETA="2"
TYPE_NUM_RELEASE="3"

#*********************************************************
# functions
#*********************************************************

# _____________________________________________________________________________________________
function displayHelpMessage() {
    echo "
***DEPRECATED***
DO NOT USE THIS TOOL RIGHT NOW
I've changed the versioning strategy for the app and need to update this
tool to reflect that.
***DEPRECATED***

This tool is for bumping the project version. It is meant to be used
manually and should be called from the repo root as ./tools/bumpver.sh.
The version should only ever be updated from this script, as this script
cannot recover from invalidly formatted version numbers in the gradle 
file.

bumpver will create a commit of its changes. If there are any existing
staged or unstaged changes in the working tree bumpver will cancel without
changing anything. If the option --stage is used, bumpver will work with
those existing changes.

Usage:
bumpver
bumpver major|minor|fix|build|beta|release [options]

<no args>
    Using bumpver with no args will simply display the current version.

major|minor|fix|build|beta|release
    Use one of these commands to increment the related version number. 
    Lesser version numbers are reset as needed.
    examples:
        major: 1.2-alpha.34 -> 2.0-alpha.1
        minor: 1.2-alpha.34 -> 1.3-alpha.1
        fix: 1.2.3 -> 1.2.4
        build: 1.2-alpha.3 -> 1.2-alpha.4
               1.2.3-beta.4 -> 1.2.3-beta.5
        beta: 1.2-alpha.3 -> 1.2.0-beta.1
              1.2.3 -> 1.2.4-beta.1
        release: 1.2.3-beta.4 -> 1.2.3

    These commands are restricted by which version types they are
    allowed to bump from:
        major: alpha
        minor: alpha
        fix: release
        build: alpha, beta
        beta: alpha, release
        release: beta
    If you try to bump a version with an incompatible command, you will
    get an error message.

Options:
-h --help       Display this help message.
-s --stage      By default, bumpver will create a commit of the version
                information changes. Use this option to only stage those
                changes without committing."
}

# _____________________________________________________________________________________________
function getCurrentVersionCode() {
    # load gradle file contents; find versionCode line; remove all non-number chars
    cat "$APP_GRADLE_FILEPATH" | grep "versionCode" | sed -E 's/[^0-9]//g'
}

# _____________________________________________________________________________________________
function getCurrentVersionName() {
    # load gradle file contents; find versionName line; isolate quoted name; remove quotes
    cat "$APP_GRADLE_FILEPATH" | grep "versionName" | grep -o "\".*\"" | sed 's/"//g'
}

# _____________________________________________________________________________________________
function workingTreeIsDirty() {
    if [[ -n "$(git status --porcelain)" ]]; then
        return 0
    else
        return 1
    fi
}

# _____________________________________________________________________________________________
# Get the major version number from a version code.
#
# Args:
# 	versionCode: the version code
# Stdout:
# 	the major number
#
function getMajor() {
    local codeLength="${#1}"
    if [[ "$codeLength" -lt "8" ]]; then
        # The code (MmmHHTBB) would only have less than 8 digits if the major 
        # version is zero.
        # eg 0.12.34-beta.5 is 1234205 <--- 7 digits
        echo "0"
    else
        # if the major number is not 0, return M
        echo "${1:0:1}"
    fi
}

# _____________________________________________________________________________________________
# Get the minor version number from a version code.
#
# Args:
# 	versionCode: the version code
# Stdout:
# 	the minor number
#
function getMinor() {
    local codeLength="${#1}"
    if [[ "$codeLength" -eq "6" ]]; then
        # eg 0.1-alpha.2 -- 100102
        echo "${1:0:1}"
    else
        # eg 0.12-alpha.3 -- 1200103
        # eg 1.2-alpha.3 -- 10200103

        # remove the leading zero
        echo "${1: -7:2}" | sed -E 's/^0?//'        
    fi
}

# _____________________________________________________________________________________________
# Get the hotfix version number from a version code.
#
# Args:
# 	versionCode: the version code
# Stdout:
# 	the hotfix number
#
function getFix() {
    # remove the leading zero
    echo "${1: -5:2}" | sed -E 's/^0?//'
}

# _____________________________________________________________________________________________
# Get the version type from a version code.
# 1=alpha; 2=beta; 3=release
#
# Args:
# 	versionCode: the version code
# Stdout:
# 	the version type int
#
function getType() {
    echo "${1: -3:1}"
}

# _____________________________________________________________________________________________
# Get the build number from a version code.
#
# Args:
# 	versionCode: the version code
# Stdout:
# 	the build number
#
function getBuild() {
    # get the last 2 characters of the arg (version code: MmmHHTBB)
    # remove the leading zero
    echo "${1: -2:2}" | sed -E 's/^0?//'
}

# _____________________________________________________________________________________________
# Checks whether or not the given command is restricted by the given version type.
# Commands are restricted by which version types they are
# allowed to bump from:
#     major: alpha
#     minor: alpha
#     fix: release
#     build: alpha, beta
#     beta: alpha, release
#     release: beta
#
# Args:
# 	command: the user input command to check
#   vertype: the version type to compare to the command
# Returns:
# 	0 if true, 1 if false
#
function isValidCommand() {
    local userCommand="$1"
    local versionType="$2"
    case "$userCommand" in
        major)
            if (( $versionType == 1 )); then return 0; else return 1; fi
        ;;
        minor)
            if (( $versionType == 1 )); then return 0; else return 1; fi
        ;;
        fix)
            if (( $versionType == 3 )); then return 0; else return 1; fi
        ;;
        build)
            if (( $versionType == 1 )); then return 0; fi
            if (( $versionType == 2 )); then return 0; fi
            return 1
        ;;
        beta)
            if (( $versionType == 1 )); then return 0; fi
            if (( $versionType == 3 )); then return 0; fi
            return 1            
        ;;
        release)
            if (( $versionType == 2 )); then return 0; else return 1; fi
        ;;
    esac
}

# _____________________________________________________________________________________________
function commandExists() {
    case "$1" in
        major|minor|fix|build|beta|release)
            return 0
        ;;
        *)
            return 1
        ;;
    esac
}

# _____________________________________________________________________________________________
function compileVersionCode() {
    local majorNum="$1"
    local minorNum="$2"
    local fixNum="$3"
    local typeNum="$4"
    local buildNum="$5"

    if [[ "$majorNum" == "0" ]]; then majorNum="" ; fi
    if [[ ( -n "$majorNum" ) && ( "${#minorNum}" == "1" ) ]]; then minorNum="0${minorNum}"; fi
    if [[ "${#fixNum}" == "1" ]]; then fixNum="0${fixNum}"; fi
    if [[ "${#buildNum}" == "1" ]]; then buildNum="0${buildNum}"; fi

    echo "${majorNum}${minorNum}${fixNum}${typeNum}${buildNum}"
}

# _____________________________________________________________________________________________
function formatVersionName() {
    local majorNum="$1"
    local minorNum="$2"
    local fixNum="$3"
    local typeNum="$4"
    local buildNum="$5"

    local formattedFixNum=""
    if [[ "$typeNum" != "$TYPE_NUM_ALPHA" ]]; then formattedFixNum=".${fixNum}"; fi
    local formattedType=""
    if [[ "$typeNum" == "$TYPE_NUM_ALPHA" ]]; then formattedType="-alpha"; fi
    if [[ "$typeNum" == "$TYPE_NUM_BETA" ]]; then formattedType="-beta"; fi
    local formattedBuildNum=""
    if [[ "$typeNum" != "$TYPE_NUM_RELEASE" ]]; then formattedBuildNum=".${buildNum}"; fi

    echo "${majorNum}.${minorNum}${formattedFixNum}${formattedType}${formattedBuildNum}"       
}

# _____________________________________________________________________________________________
function updateGradleVersion() {
    local newVersionCode="$1"
    local newVersionName="$2"

    # /<line-filter>/ s/<old-code>/<new-code>/
    # The line filter is used to avoid comments - it only lets through lines which 
    # start w/ whitespace (^\s+).
    sed -i -E "/^\s+versionCode/ s/[0-9]+/${newVersionCode}/" "$APP_GRADLE_FILEPATH"

    # /<line-filter>/ s/<old-name>/<new-name>/
    sed -i -E "/^\s+versionName/ s/\".*\"/\"${newVersionName}\"/" "$APP_GRADLE_FILEPATH"
}

# _____________________________________________________________________________________________
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

#*********************************************************
# main
#*********************************************************

# _____________________________________________________________________________________________
function main() {
    # if no args, just display the current version and exit
    if [[ -z "$@" ]]; then
        getCurrentVersionName
        getCurrentVersionCode
        exit 0
    fi

    # ________________________________________ parse args
    local optionStage="false"
    local positionalArgs=()

    for arg in "$@"; do
        case "$arg" in
            -s|--stage)
                optionStage="true"; shift
            ;;
            -h|--help)
                displayHelpMessage
                exit 0
            ;;
            *)
                positionalArgs+=("$arg"); shift
            ;;
        esac
    done

    if [[ ${#positionalArgs[@]} -ne 1 ]]; then
        echo "Error: too many positional arguments (expected 1)" >&2
        exit 1
    fi

    if workingTreeIsDirty; then
        # TODO this condition should be ANDed w/ above but I'm braindead
        if [[ "$optionStage" == "false" ]]; then
            echo "Error: There are uncommitted changes in the working tree. Please commit or reset, then try again." >&2
            exit 1
        fi
    fi

    # ________________________________________ bump the version number

    bumpCommand="${positionalArgs[0]}"
    if ! commandExists "$bumpCommand"; then
        echo "Error: Unrecognized command '${bumpCommand}'" >&2
        exit 1
    fi

    # get the current version code
    local currentVersionCode="$(getCurrentVersionCode)"

    # split into the indiv numbers
    local majorNum="$(getMajor "${currentVersionCode}")"
    local minorNum="$(getMinor "${currentVersionCode}")"
    local fixNum="$(getFix "${currentVersionCode}")"
    local typeNum="$(getType "${currentVersionCode}")"
    local buildNum="$(getBuild "${currentVersionCode}")"
    
    # check that the number being bumped is valid for the type
    if ! isValidCommand "$bumpCommand" "$typeNum" ; then
        echo "Error: command '${bumpCommand}' is not compatible with the version-type of $(getCurrentVersionName)" >&2
        echo "See 'bumpver --help' for more information." >&2
        exit 1
    fi

    # update the numbers as needed (new version code)
    case "$bumpCommand" in
        major)
            # eg 1.2-alpha.34 -> 2.0-alpha.1
            majorNum="$(($majorNum + 1))"
            minorNum="0"
            fixNum="0"
            buildNum="1"
        ;;
        minor)
            # eg 1.2-alpha.34 -> 1.3-alpha.1
            minorNum="$(($minorNum + 1))"
            fixNum="0"
            buildNum="1"            
        ;;
        fix)
            # eg 1.2.3 -> 1.2.4
            fixNum="$(($fixNum + 1))"
        ;;
        build)
            # eg 1.2-alpha.3 -> 1.2-alpha.4
            #    1.2.3-beta.4 -> 1.2.3-beta.5
            buildNum="$(($buildNum + 1))"
        ;;
        beta)
            # eg 1.2-alpha.3 -> 1.2.0-beta.1
            #    1.2.3 -> 1.2.4-beta.1
            if [[ "$typeNum" == "$TYPE_NUM_RELEASE" ]]; then
                fixNum="$(($fixNum + 1))"
            fi        
            typeNum="$TYPE_NUM_BETA"
            buildNum="1"
        ;;
        release)
            # eg 1.2.3-beta.4 -> 1.2.3
            typeNum="$TYPE_NUM_RELEASE"
            buildNum="0"
        ;;
    esac    

    # create the new code & name
    local newVersionCode="$(compileVersionCode ${majorNum} ${minorNum} ${fixNum} ${typeNum} ${buildNum})"
    local newVersionName="$(formatVersionName ${majorNum} ${minorNum} ${fixNum} ${typeNum} ${buildNum})"

    # write code & name to file
    if ! getUserConfirmation "$(getCurrentVersionName)" "$newVersionName"; then
        echo "Aborting version bump operation."
        exit 0
    fi
    updateGradleVersion "$newVersionCode" "$newVersionName"

    # commit changes
    if [[ "$optionStage" == "false" ]]; then
        git add --all
        git commit -m "chore(version): Bump version to ${newVersionName}" --quiet --no-verify
        git tag -a "v${newVersionName}" -m ""
    fi

    echo
    if [[ "$optionStage" == "false" ]]; then 
        echo "Committed changes.";
    else 
        echo "Staged changes without committing."
    fi
    echo "Bumped version to ${newVersionName}"
}

#*********************************************************
# run
#*********************************************************

set -e -u -o pipefail
main "$@"