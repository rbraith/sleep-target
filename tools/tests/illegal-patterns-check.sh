#!/bin/bash
#
# Check the project files for illegal text patterns.
# This only checks files that have changed from main in git.
#
# Usage:
# illegal-patterns <config-file>
#
# <config-file>
#   This is a .cfg file containing file-type masks &
#   a list of illegal patterns to check against. See
#   example-illegal-patterns.cfg.

#*********************************************************
# functions
#*********************************************************

# Reads the contents of a section of a config file
#
# Args:
# 	configFilepath: the path of the config file to read from
#   sectionName: the name of the section to read
# Stdout:
#   The contents of the config section
function readConfigSection() {
    local configFilepath="$1"; shift
    local sectionName="^\[$@\]$"
    
    local sectionEnd="^\[[a-z ]+\]$"

    cat ${configFilepath} |\
        # remove the comments
        sed -E '/^\#/d' |\
        # replace escaped line-starting hashes
        sed -E 's/^\\\#/\#/g' |\
        # find section contents (lines between 2 sections inclusive) 
        sed -E -n "/${sectionName}/,/${sectionEnd}/p" |\
        # remove included section start & end
        # remove empty lines        
        sed -E "/${sectionName}/d;/${sectionEnd}/d" | grep -E -v '^$'
}

# gets all staged files matching the given pattern
#
# Args:
# 	files_pattern: a regex pattern to filter staged files by
#
# Stdout:
# 	the matching files
#
function getReleventFiles() {
    local files_pattern="$@"

    # list the staged files
    git diff main --name-only --diff-filter=drt |\
        # prune the files based on the filetypes
        grep -E "${files_pattern}"
}

#---------------------------------------------------------
# main
#---------------------------------------------------------

function main() {
    set -e -o pipefail

    # ________________________________________ parse config file
    local config_file="$1"
    
    local filetypes=()
    while read line; do filetypes+=("$line"); done < <( readConfigSection "${config_file}" 'file types' )

    # TODO escape backslashes (& dollar signs?) in each illegal pattern?
    local illegal_patterns=()
    while read line; do illegal_patterns+=("$line"); done < <( readConfigSection "${config_file}" 'illegal patterns' )

    # ________________________________________ collect relevent files
    local joined_filetypes="${filetypes[0]}" # eg xml|java|kt
    for ((i = 1; i < ${#filetypes[@]}; i++)); do joined_filetypes="${joined_filetypes}|${filetypes[i]}"; done
    local files_pattern="\\.\\b($joined_filetypes)\\b\$"

    local relevent_files=()
    while read line; do relevent_files+=("$line"); done < <( getReleventFiles "$files_pattern" )   


    # ________________________________________ run illegal pattern check
    export GREP_COLORS='mt=31:ln=33:se=90:fn=97'
        # GREP_COLORS env var
        # https://dom111.github.io/grep-colors
        # https://superuser.com/a/358903
        # https://www.gnu.org/software/grep/manual/html_node/Environment-Variables.html
        # https://askubuntu.com/questions/1042234/modifying-the-color-of-grep
        #       ^^^refer to the 'non-GUI TTY' columns of the tables

    violations=0
    # using indices instead of for-each pattern, since for-each doesn't
    # seem to work with elements containing spaces
    for ((i = 0; i < ${#relevent_files[@]}; i++)); do
        file="${relevent_files[i]}"
        for ((j = 0; j < ${#illegal_patterns[@]}; j++)); do
            illegal_pattern="${illegal_patterns[j]}"
            grep -Hn --color=always -E "$illegal_pattern" "$file" && ((violations += 1))
        done
    done

    # return whether there were violations
    [[ $violations -eq 0 ]]  
}


#*********************************************************
# script
#*********************************************************

main $@
