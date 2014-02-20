# Git Branch-Health Tool
#
# A tool for showing, with colors, the health of branches, both locally and
# on remotes, of a specified git repository. The health of a branch is
# computed using the time since last activity was recorded on the branch. This
# can be specified on the command line.
#
# Inspired by Felipe Kiss' "Show Last Activity On Branch" script, available at:
# https://coderwall.com/p/g-1n9w
#

from prettylogger import PrettyLogger
from git import *
import argparse
import sys
from datetime import *
import dateutil.parser
from colors import red, yellow, green

# Use to turn on debugging output
DEBUG = False

# Use to turn on verbose output
VERBOSE = False

# Whether or not color formatting should be turned on
COLOR = True

# Constants specifying branch health
HEALTHY = 0
AGED = 1
OLD = 2

gLog = None
gParser = None

# Show the health of all branches in a git repository with a given path.
#
# @param aRepoPath The path to the git repository for which branch health
#        information is desired.
# @param aRemoteName The name of the remote of repository whose local repository
#        is located at aRepoPath, on which to operate. Note that specifying
#        'None' for this parameter will cause it to operate on the local version
#        of the repository.
# @param aHealthyDays The number of days that a branch can be untouched and
#        still be considered 'healthy'.
# @param aOptions Display options specified on the command line.

def showBranchHealth(aRepoPath, aRemoteName, aHealthyDays, aOptions):
  global gLog

  branchMap = []

  gLog.debug('Operating on repository in: ' + aRepoPath)
  gLog.debug('Operating on remote named: ' + aRemoteName)

  remotePrefix = ''
  if aRemoteName:
    remotePrefix = 'remotes/' + aRemoteName + '/'

  repo = Repo(aRepoPath)
  gitCmd = Git(aRepoPath)
  assert(repo.bare == False)

  remoteToUse = None

  for someRemote in repo.remotes:
    if aRemoteName == someRemote.name:
      remoteToUse = someRemote

  if remoteToUse:
    for branch in remoteToUse.refs:
      branchName = remotePrefix + branch.remote_head
      hasActivity = gitCmd.log('--abbrev-commit', '--date=relative', '-1', branchName)
      hasActivityNonRel = gitCmd.log('--abbrev-commit', '--date=iso', '-1', branchName)
      hasActivityLines = hasActivity.split('\n')
      hasActivityNonRelLines = hasActivityNonRel.split('\n')
      i = 0
      for line in hasActivityLines:
        if 'Date:' in line:
          lastActivity = line.replace('Date: ', '').strip()
          lastActivityNonRel = hasActivityNonRelLines[i].replace('Date: ', '').strip()
          break
        i = i + 1

      branchMap.append((branchName, (lastActivity, lastActivityNonRel)))

    sortedBranches = sortBranchesByHealth(branchMap, aHealthyDays)
    printBranchHealthChart(sortedBranches, aOptions)

# Comparison function to compare two branch tuples.
#
# @param aTupleList1 A branch tuple containing the following:
#        1) The branch name and 2) A date tuple, with each tuple continaing the
#        following: 2a) A human-readable date (e.g. '2 days ago'), and 2b) an
#        iso-standardized date for comparison with other dates. Note that 2a and
#        2b should be equivalent, with 2a being less accurate, but more easily
#        interpretable by humans.
# @param aTupleList2 A second branch tuple, with the same specification as
#        aTupleList1 which should be compared to aTupleList1.
#
# @returns -1, If the branch represented by aTupleList1 is older than the branch
#          represented by aTupleList2; 1 if the branch represented by
#          aTupleList2 is older than the branch represented by aTupleList1;
#          0, otherwise.

def isoDateComparator(aTupleList1, aTupleList2):
  (branchName1, valueTuple1) = aTupleList1
  (branchName2, valueTuple2) = aTupleList2

  (humanDate1, isoDate1) = valueTuple1
  (humanDate2, isoDate2) = valueTuple2

  if isoDate1 < isoDate2:
    return -1
  elif isoDate1 == isoDate2:
    return 0

  return 1

# Sort a list of branch tuples by the date the last activity occurred on them.
#
# @param aBranchList A list of tuples, with each tuple having the following:
#        1) The branch name and 2) A date tuple, with each tuple continaing the
#        following: 2a) A human-readable date (e.g. '2 days ago'), and 2b) an
#        iso-standardized date for comparison with other dates. Note that 2a and
#        2b should be equivalent, with 2a being less accurate, but more easily
#        interpretable by humans.
# @param aHealthyDays The number of days that a branch can be untouched and
#        still be considered 'healthy'.
#
# @returns A list of tuples, with each tuple having the following:
#        1) The branch name and 2) A date tuple, with each tuple continaing the
#        following: 2a) A human-readable date (e.g. '2 days ago'), and 2b) an
#        iso-standardized date for comparison with other dates. Note that 2a and
#        2b should be equivalent, with 2a being less accurate, but more easily
#        interpretable by humans. This list is guaranteed to be sorted in non-
#        ascending order, by the iso-standardized date (#2b, above).

def sortBranchesByHealth(aBranchMap, aHealthyDays):
  global gLog

  sortedBranchMap = sorted(aBranchMap, cmp=isoDateComparator)

  return markBranchHealth(sortedBranchMap, aHealthyDays)

# Traverse a list of branch tuples and mark their healthy status.
#
# @param aBranchList A list of tuples, with each tuple having the following:
#        1) The branch name and 2) A date tuple, with each tuple continaing the
#        following: 2a) A human-readable date (e.g. '2 days ago'), and 2b) an
#        iso-standardized date for comparison with other dates. Note that 2a and
#        2b should be equivalent, with 2a being less accurate, but more easily
#        interpretable by humans.
# @param aHealthyDays The number of days that a branch can be untouched and
#        still be considered 'healthy'.
#
# @returns A list of simplified tuples, each containing: 1) the branch name,
#          2) the human-readable date since last activity on the branch (#2a
#          above), and 3) a constant indicating the health status of the branch

def markBranchHealth(aBranchList, aHealthyDays):
  finalBranchList = []
  # Compute our time delta from when a branch is no longer considered
  # absolutely healthy, and when one should be pruned.
  for branchTuple in aBranchList:
    (branchName, dateTuple) = branchTuple
    (humanDate, isoDate) = dateTuple
    branchdate = dateutil.parser.parse(isoDate)
    branchLife = date.today() - branchdate.date()
    if branchLife > timedelta(aHealthyDays * 2):
      branchHealth = OLD
    elif branchLife > timedelta(aHealthyDays):
      branchHealth = AGED
    else:
      branchHealth = HEALTHY

    finalBranchList.append((branchName, humanDate, branchHealth))

  return finalBranchList

# Print out a 'health chart' of different branches, and when they were last
# changed. The health chart will color the given branches such that:
#     - Branches with last activity longer than double the number of 'healthy
#       days' ago will be colored in RED.
#     - Branches with last activity longer than the number of 'healthy days'
#       ago will be colored in YELLOW.
#     - All other branches will be colored in GREEN
#
# @param aBranchMap A list of tuples where each tuple contains 1) the name
#        of a branch, 2) the last activity (in human readable format), and 3)
#        the constant indicating the health of this branch, computed from
#        the original, iso-standardized date.
# @param aOptions Display options specified on the command line.

def printBranchHealthChart(aBranchMap, aOptions):
  (badOnly) = aOptions

  for branchTuple in aBranchMap:
    (branchName, lastActivityRel, branchHealth) = branchTuple

    # Skip healthy and aged branches if we're only looking for bad ones
    if badOnly and not branchHealth == OLD:
      continue

    if branchHealth == HEALTHY:
      coloredDate = green(lastActivityRel)
    elif branchHealth == AGED:
      coloredDate = yellow(lastActivityRel)
    else:
      coloredDate = red(lastActivityRel)

    alignedPrintout = '{0:40} {1}'.format(branchName + ":", coloredDate)
    print(alignedPrintout)

# Construct an argparse parser for use with this program to parse command
# line arguments.
#
# @returns An argparse parser object which can be used to parse command line
#          arguments, specific to git-branchhealth.

def createParser():
  parser = argparse.ArgumentParser(description='''
     Show health (time since creation) of git branches, in order.
  ''', add_help=True)
  parser.add_argument('-V', '--verbose', action='store_true', dest='verbose',
                      help='Output as much as possible')
  parser.add_argument('-r', '--remote', metavar=('<remote name>'), action='store',
                      help='Operate on specified remote', default='origin',
                      dest='remote')
  parser.add_argument('-b', '--bad-only', action='store_true',
                      help='Only show branches that are ready for pruning (i.e. older than numDays * 2)',
                      dest='badOnly')
  parser.add_argument('-d', '--days', action='store', dest='numDays',
                      help='Specify number of days old where a branch is considered to no longer be \'healthy\'',
                      default=14)
  parser.add_argument('repo', help='Path to git repository where branches should be listed',
                      nargs='?')

  return parser

# Parse arguments given on the command line. Uses the argparse package to
# perform this task.
def parseArguments():
  global gParser, VERBOSE, DEBUG

  if not gParser:
    gParser = createParser()

    parsed = gParser.parse_args(sys.argv[1:])

  if parsed.verbose:
    VERBOSE=True
    DEBUG=True

  options = (parsed.badOnly)

  return (parsed.repo, parsed.remote, parsed.numDays, options)

# Main entry point
def runMain():
  global gParser, gLog, DEBUG, VERBOSE

  (repo, remote, numHealthyDays, options) = parseArguments()
  gLog = PrettyLogger(COLOR, DEBUG, VERBOSE)

  if repo == None:
    gParser.print_help()
    return

  showBranchHealth(repo, remote, int(numHealthyDays), options)

if __name__ == '__main__':
  runMain()
