# PythonDCE
Pycharm Plugin useful for detecting potentially dead code in Python. This plugin adds three inspections for detecting
potentially unused classes, functions (including methods) and names (in global and class-level assignments). 
The inspections are more reliable if the code base contains type hints and does not make use of Python dynamic features.
They are experimental, rather inefficient and are essentially equivalent to running 'Find usages' on each symbol by hand.

# Updates
v 0.2
-----
* Added a new inspection which checks a function or method and highlights if a parameter has the same constant value at
all call sites. The inspection still has some false positives and does not provide a quick fix for replacing the
parameter with that constant value.