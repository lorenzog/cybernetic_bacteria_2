#!/bin/bash
while read line ; do echo "$line" | curl -T - localhost:8000 ;done < inputStrings.txt
