# Main
javac gitlet/Main.java
java gitlet.Main init
java gitlet.Main add <filename>
java gitlet.Main commit <message>
java gitlet.Main checkout <commit id> -- <filename>
java gitlet.Main checkout -- <filename>

# DumpCommit
javac gitlet/DumpCommit.java
java gitlet.DumpCommit <hash>

# Testing
- `/proj2` dir 
  - Recompile Main
    - javac gitlet/Main.java
  - Recompile all .java files
    -  javac gitlet/*.java
- `/testing` dir
  - Individual Test
    - python3 tester.py --verbose student_tests/<.in file>
    - python3 tester.py --verbose --keep student_tests/<.in file>
  - All Tests
    - make check