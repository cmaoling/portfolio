#!/bin/bash

# Define the base directory
BASE_DIR="."

# Find all .java files in the directory tree and loop through them
find "$BASE_DIR" -type f -name "*.java" | while read -r file; do
    # Use sed to replace all instances of "javax" with "jakarta"
    sed -i 's/import com\.ibm\.icu\./import java./g' "$file"
done
