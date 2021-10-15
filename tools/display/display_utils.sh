#!/bin/bash

#*********************************************************
# imports
#*********************************************************

source "./tools/display/styles.sh" || exit 1

#*********************************************************
# functions
#*********************************************************

# colorizes & displays a message
#
# Args:
# 	clr_command: the color command with which to colorize the output
#       (see styles.sh for the various color commands)
#   message: the text to display, newline characters are allowed
#
# Stdout:
# 	the colorized message
#
function display_clr() {
    # https://stackoverflow.com/a/13484149
    # fix for bug where echoing '*' would print filelist of current dir
    GLOBIGNORE="*"

    local clr_command="$1"; shift

    echo -e "$( $clr_command )$@$( clr_reset )"
}
