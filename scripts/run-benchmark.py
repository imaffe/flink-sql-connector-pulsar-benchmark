#!/usr/bin/env python3


import subprocess

bashCommand = "cwm --rdf test.rdf --ntriples > test.nt"
process = subprocess.Popen(bashCommand.split(), stdout=subprocess.PIPE)
output, error = process.communicate()