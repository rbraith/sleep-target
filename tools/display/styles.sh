#!/bin/bash
#
# holds various styles for other scripts

# https://stackoverflow.com/a/20983251
# https://unix.stackexchange.com/questions/269077/tput-setaf-color-table-how-to-determine-color-codes


function clr_red() {
    tput setaf 1
}

function clr_green() {
    tput setaf 2
}

function clr_yellow() {
    tput setaf 3
}

function clr_cyan() {
    tput setaf 6
}

function clr_reset() {
    tput sgr0
}