from setuptools import setup
import os

progName = 'proton-pack'
progVersion = '1.0.7'
progDescription='A suite of general development tools that help programmers automate mundane tasks.'
progAuthor = 'Scott Johnson'
progEmail = 'jaywir3@gmail.com'
progUrl = 'http://github.com/jwir3/proton-pack'
entry_points = { 'console_scripts': [
  'git-branchhealth = gitbranchhealth.BranchHealth:runMain',
  'genregression = genregression.regression:cli',
]}

setup(name=progName,
      version=progVersion,
      description=progDescription,
      author=progAuthor,
      author_email=progEmail,
      url=progUrl,
      packages=['gitbranchhealth', 'genregression', 'prettylogger'],
      entry_points=entry_points,
      install_requires=['argparse', 'ansicolors', 'httplib2', 'mozfile',
                        'mozprofile', 'mozrunner', 'BeautifulSoup']
)
