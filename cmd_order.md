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
