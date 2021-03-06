#  Copyright (c) 2020-2021 Rich Braithwaite (rbraithwaite.dev@gmail.com)
#
#  This program is free software: you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published by
#  the Free Software Foundation, either version 3 of the License, or
#  (at your option) any later version.
#
#  This program is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#
#  You should have received a copy of the GNU General Public License
#  along with this program.  If not, see <https://www.gnu.org/licenses/>.

import argparse
import os
import subprocess
import sys

DESCRIPTION = """
This script works in conjunction with the 'rearrange' Android Studio
reformatting operation. That operation puts the files into an
intermediate form which is processed by this script.

The rationale for this script is the annoying restrictiveness of
the 'section rules' in the rearrange options -- you can only have
single line header comments, and they must be accompanied by an end-
section comment.

NOTE: This script only works on java files.

The typical procedure should be:
    1. Run code reformatting & import optimization in Android Studio
    2. Run code rearrange in Android Studio
    3. Run this script on the same code that was reformatted

Intermediate form syntax (used in the Android Studio arrangement section rules):
    //!section! properties
        replaced with a section header comment
        eg:
        //*********************************************************
        // properties
        //*********************************************************

    //!end! properties
        Section rules require an ending tag.
        Right now the matching name with the start tag serves no
        purpose other than to make the end tag unique.
        These lines are deleted.

The first thing this script does is remove existing headers. It then 
processes the section rule comments and adds the new headers.
Obnoxiously, section rules are generated recursively for inner classes, etc.
This script deletes those inner sections.

NOTE: It's important that the options passed to this script match
the ones used to reformat and rearrange the code. (ie, recursive
subdirectories and VCS mask)
"""

USAGE = "python -m fix-sections <path>... [options]"

PREFIX_SECTION_HEADER = "//!section!"
PREFIX_SECTION_END = "//!end!"

SECTION_HEADER_LINE_THINGY = "//*********************************************************"

#*********************************************************
# functions
#*********************************************************

#_____________________________________________________________________________________________
def initArgParser():
    """Initializes an arg parser with the arguments of this script."""

    parser = argparse.ArgumentParser(
        description=DESCRIPTION,
        usage=USAGE,
        formatter_class=argparse.RawDescriptionHelpFormatter
    )
    parser.add_argument("path", nargs="+", help="Can be a directory or file. Should be absolute. Should match what was reformatted in Android Studio.")
    parser.add_argument("-s", "--sub", action="store_true", help="If the path is a directory, apply this script recursively to all subdirs.")
    parser.add_argument("-v", "--vcs", action="store_true", help="Only apply this script to changed files in the repo index.")
    parser.add_argument("-c", "--clear", action="store_true", help="Delete existing section headers then quit.")
    parser.add_argument("-C", "--check", action="store_true", help="Check for files containing intermediate tags.")
    parser.add_argument("-q", "--quiet", action="store_true", help="Prints the filenames of failed files, otherwise prints nothing.")

    return parser

#_____________________________________________________________________________________________
def validatePathExists(path:str):
    """If the path does not exist, script exits with an error message."""

    if not os.path.exists(path):
        print(f"Error: Invalid path: {path}", file=sys.stderr)
        sys.exit(1)

#_____________________________________________________________________________________________
def collectJavaFiles(paths:list, searchSubdirs:bool, vcsOnly:bool)->set:
    """Collects a list of java files which match the provided args, and have been rearranged"""

    files = set()

    for path in paths:
        # path is file
        if os.path.isfile(path):
            files.add(os.path.abspath(path))
        # path is dir
        elif searchSubdirs: 
            files.update(getJavaFilesFromTree(path))
        else: 
            files.update(getJavaFilesFromDir(path))

    if vcsOnly: files = filterVcsOnly(files)

    files = filterRearrangedOnly(files)

    return files

#_____________________________________________________________________________________________
def isJavaFile(filepath:str)->bool:
    return filepath.endswith(".java")

#_____________________________________________________________________________________________
def isSectionHeader(line:str)->bool:
    return line.strip().startswith(PREFIX_SECTION_HEADER)

#_____________________________________________________________________________________________
def isSectionEnd(line:str)->bool:
    return line.strip().startswith(PREFIX_SECTION_END)


#_____________________________________________________________________________________________
def isExistingHeaderStart(line:str)->bool:
    return line.strip().startswith(SECTION_HEADER_LINE_THINGY)

# _____________________________________________________________________________________________
def lineIsBlockStart(line:str): 
    return line.strip() == "{"

#_____________________________________________________________________________________________
def getJavaFilesFromTree(rootDir:str)->list:
    result = []
    for foldername, subfolders, filenames in os.walk(rootDir):
        result += [os.path.join(os.path.abspath(foldername), f) for f in filenames if isJavaFile(f)]
    return result

#_____________________________________________________________________________________________
def getJavaFilesFromDir(d:str)->list:
    filenames = os.listdir(d)
    return [os.path.join(os.path.abspath(d), f) for f in filenames if isJavaFile(f)]

#_____________________________________________________________________________________________
def filterVcsOnly(fileList:set)->set:
    """only returns files which have changed from the last commit"""

    changedFiles = getChangedJavaFilesInRepo()
    return {f for f in fileList if f in changedFiles}


#_____________________________________________________________________________________________
def getChangedJavaFilesInRepo()->list:
    gitOutput = str(
        subprocess.check_output("git diff --name-only".split(), stderr=subprocess.DEVNULL),
        encoding="utf-8")
    javaFiles = [os.path.abspath(filename) for filename in gitOutput.split() if isJavaFile(filename)]

    return javaFiles

#_____________________________________________________________________________________________
def filterRearrangedOnly(fileList:set)->set:
    def fileWasRearranged(line:str)->bool:
        return isSectionHeader(line)

    result = set()
    for f in fileList:
        with open(f) as javaFile:
            for line in javaFile:
                if fileWasRearranged(line):
                    result.add(f)
                    break
    return result

# _____________________________________________________________________________________________
def processFile(filepath:str):
    """Responsible for actually processing the section headers in the file, and saving the updated file"""

    lines = []
    with open(filepath) as f:
        lines = f.readlines()       
        lines = clearExistingSectionHeaders(lines)       
        lines = processSectionRules(lines)

    with open(filepath, "w") as f:
        f.writelines(lines)


#_____________________________________________________________________________________________
def clearExistingSectionHeaders(lines:list)->list:
    updatedLines = []
    
    i = 0
    linesLength = len(lines)
    while i < linesLength:
        line = lines[i]

        if isExistingHeaderStart(line):
            # skip existing header
            i += 3
            # fix for bug where fix-sections keeps adding more blank lines on iterative calls
            # (basically, remove instances of blank lines added from processSectionRules())
            if i < linesLength and not lines[i].isspace():
                updatedLines.append(lines[i])
        else:
            # retain all other lines
            updatedLines.append(line)

        i += 1

    return updatedLines


#_____________________________________________________________________________________________
def formatSectionHeaderLines(title:str)->list:
    return [
        SECTION_HEADER_LINE_THINGY + "\n",
        f"// {title}\n",
        SECTION_HEADER_LINE_THINGY + "\n"
    ]

#_____________________________________________________________________________________________
def extractTitle(sectionHeader:str)->str:
    result = sectionHeader.strip()[len(PREFIX_SECTION_HEADER):]
    return result.strip()

#_____________________________________________________________________________________________
def isValidRearrangedFile(filepath)->bool:
    # Search for the outer class section rule.
    # For some reason some rearranged files do not have this rule generated, 
    # and this script is expecting that rule.
    with open(filepath) as f:
        sectionHeaderWasFound = False
        for line in f.readlines():
            if lineIsBlockStart(line):
                return sectionHeaderWasFound
            elif isSectionHeader(line):
                sectionHeaderWasFound = True
    return True # usually when the file is a simple empty class, with a one-line block (in these cases block start isn't found)

#_____________________________________________________________________________________________
def processSectionRules(lines:str)->list:
    """replace section rule intermediate comments"""

    updatedLines = []

    tagStack = []
    def stackIsEmpty(stack): return (len(stack) == 1) # == 1 so that first outer class header is ignored as well
    def growStack(stack): stack.append(None)
    def popStack(stack): stack.pop()

    prevLine = None
    for line in lines:
        if isSectionHeader(line):
            if stackIsEmpty(tagStack):
                if (prevLine is not None 
                        and not prevLine.isspace()
                        and not lineIsBlockStart(prevLine)): # no blank line at the start of the class
                    updatedLines.append("\n") # ensure blank line before section header
                updatedLines += formatSectionHeaderLines(extractTitle(line))
                updatedLines.append("\n") # blank line
            growStack(tagStack)
        elif isSectionEnd(line):
            popStack(tagStack)
        # Remove any blank lines from code block starts
        # TODO This is probably a formatting concern beyond the responsibility of this script
        # It was just convenient at the time to have this logic here, while I'm iterating 
        # through these files.
        elif not (
            (prevLine is not None and lineIsBlockStart(prevLine)) 
            and line.isspace()
            ):
            # retain all normal lines
            updatedLines.append(line)

        prevLine = line
            

    return updatedLines


#*********************************************************
# main
#*********************************************************

if __name__ == "__main__":
    parser = initArgParser()
    args = parser.parse_args()

    for p in args.path:
        validatePathExists(p)

    filelist = collectJavaFiles(args.path, args.sub, args.vcs)

    # ________________________________________ check for rearranged files
    if args.check:
        for f in filelist: print(f)
        sys.exit()

    # ________________________________________ clear headers only
    if args.clear:
        for f in filelist:
            updatedLines = []
            with open(f) as ff:
                updatedLines = clearExistingSectionHeaders(ff.readlines())
            with open(f, "w") as ff:
                ff.writelines(updatedLines)
                
        print("\n".join([
            "Cleared section headers in these files:",
            "---------------------------------------------------------"
        ]))
        for f in fixedFiles: print(f)

        sys.exit()

    # ________________________________________ process files
    invalidFiles = []
    fixedFiles = []
    for f in filelist: 
        if not isValidRearrangedFile(f):
            invalidFiles.append(f)
        else:
            processFile(f)
            fixedFiles.append(f)

    if args.quiet:
        if invalidFiles:
            for f in invalidFiles: print(f, file=sys.stderr)
            sys.exit(1)
        sys.exit()

    exitCode = 0
    if invalidFiles:
        exitCode = 1
        print("\n".join([
            "Some files have unrecognized rearrange formatting. This can happen when",
            "reformatting from the project tools window. Running rearrange on these",
            "files again should fix the problem, either from the project tools window",
            "or on each file individually.",
            "---------------------------------------------------------"
        ]), file=sys.stderr)
        for f in invalidFiles: print(f, file=sys.stderr)
        print("", file=sys.stderr)

    if fixedFiles:
        print("\n".join([
            "Fixed section header comments in these files:",
            "---------------------------------------------------------"
        ]))
        for f in fixedFiles: print(f)

    sys.exit(exitCode)
