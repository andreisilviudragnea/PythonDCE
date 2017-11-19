# PythonDCE
Pycharm Plugin useful for detecting potentially dead code in Python. This plugin adds three inspections for detecting
potentially unused classes, functions (including methods) and names (in global and class-level assignments). 
The inspections are more reliable if the code base contains type hints and does not make use of Python dynamic features.
They are experimental, rather inefficient and are essentially equivalent to running 'Find usages' on each symbol by hand.