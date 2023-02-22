# Gitlet Design Document

**Name**:

## Classes and Data Structures

### Main

This is the entry point to our program. It takes in arguments from the command line and based on the command (the first
element of the args array), calls the corresponding command in `Repository` which will actually execute the logic of
the command. It also validates the arguments based on the command to ensure that enough arguments were passed in.

#### Fields

This class has no fields and hence no associated state: it simply validates arguments and defers the execution to the
`Repository` class.


### Commit

This class represents a Commit that will be stored in a file. Because each Commit will have a unique SHA-1 hash, we will
use the first two characters as the directory in the `.gitlet` folder, and the remaining characters as the filename 
within that directory as the name of the file that the object is serialized to.

All Commit objects are serialized within the `objects` folder which is within the `.gitlet` folder. The Commit class has
helpful methods that will return the Commit object corresponding to some SHA-1 hash given to it, as well as write that 
Commit to a file to persist its changes.

This class will contain the following information:
- Metadata
  - Commit log message
  - Timestamp
- Reference to a tree
  - Tree: Directory structure mapping names to references to blobs
  - NOTE: Will need a mapping data structure of some kind (TreeMap?)
- Reference to parent commit

#### Fields

1. Field 1
2. Field 2


### Reference

This class represents a Reference, which will store a commit's SHA-1 value under a simple name so that the simple name
can be used instead of the raw SHA-1 value.

The simple name will include an object for HEAD, master, as well as all the other branches.

The HEAD file is usually a symbolic reference to the branch you’re currently on. However, in some rare cases the HEAD
file may contain the SHA-1 value of a gitlet object.


### Repository

This is where the main logic of our program will live. This file will handle all the actual gitlet commands.

It will also be responsible for setting up all persistence within gitlet. This includes creating the `.gitlet` folder as
well as the folders and files where we store all Commit objects.

This class defers all Commit specific logic to the Commit class: for example, instead of having the Repository class
handle Commit serialization and deserialization, we have the Commit class contain the logic for that.

#### Fields

1. Field 1
2. Field 2


## Algorithms

## Persistence
The directory structure looks like this:

```
CWD                                                           <==== Whatever the current working directory is
└── .gitlet                                                   <==== All persistant data is stored within here
    ├── HEAD                                                  <==== Where the HEAD reference is stored (a file)
    └── objects                                               <==== All blobs and commits are stored in this directory
        ├── xx                
        ├── ...
        └── <first 2 characters of SHA-1 hash>                <==== Directory for blobs and commits
            └── <remaining 38 characters of SHA-1 hash>       <==== A Commit instance or blob stored to a file
    └── refs                                                  <==== All referenes are stored in this directory
        ├── master                                            <==== A reference containing a SHA-1 hash for a commit
        ├── ...
        └── <branch name>
```

# Notes
- Adding to the staging area creates a new SHA-1 hash for the file