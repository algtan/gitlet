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


### HashFileStructure

This class represents the directory and file information for a given SHA-1 hash.

#### Fields

1. `hashDirName`
2. `hashFileName`
3. `hashDir`
4. `hashFile`

### Repository

This is where the main logic of our program will live. This file will handle all the actual gitlet commands.

It will also be responsible for setting up all persistence within gitlet. This includes creating the `.gitlet` folder as
well as the folders and files where we store all Commit objects.

This class defers all Commit specific logic to the Commit class: for example, instead of having the Repository class
handle Commit serialization and deserialization, we have the Commit class contain the logic for that.

#### Fields

1. Field 1
2. Field 2


### Reference

While not necessarily a class, a Reference is a file that will store a commit's SHA-1 value under a simple name so that 
the simple name can be used instead of the raw SHA-1 value.

The simple name will include a file for HEAD, master, as well as all the other branches.

The HEAD file is usually a symbolic reference to the branch you’re currently on. However, in some rare cases the HEAD
file may contain the SHA-1 value of a gitlet object.

## Algorithms

### Find the latest common ancestor (split point)

The Commits in gitlet form a directed acyclic graph (DAG), where child commits have a reference to parent commits, but
the parent commits don't have any references to their children. When merging two branches, it is important to find a
latest common ancestor (split point) to serve as a baseline that helps us make a decision about how to combine the
changes together into one commit.

Steps:
1. Create a graph of all the commits in the `refs` folder
2. Traverse through the graph starting from one branch's latest commit, creating a set of all the visited vertices
   (commits)
3. Using Breadth First Search (BFS), traverse through the graph starting from the other branch's latest commit
   - Use a queue to know which vertex to visit next
   - Since we're using BFS, the vertices will be added to the queue based on distance from the child commit
   - Dequeue the next item and see if it is contained in the set of the first branch's commits
   - Once we find an item in the queue that is contained in the set, we have our split point


## Persistence
The directory structure looks like this:

```
CWD                                                           <==== Whatever the current working directory is
└── .gitlet                                                   <==== All persistant data is stored within here
    ├── blobs                                                 <==== All blobs are stored in this directory             
    │   └── <SHA-1 hash>                                      <==== A blob stored to a file
    ├── commits                                               <==== All commits are stored in this directory
    │   └── <SHA-1 hash>                                      <==== A Commit instance stored to a file
    ├── refs                                                  <==== All referenes are stored in this directory
    │   ├── master                                            <==== A reference containing a SHA-1 hash for a commit
    │   ├── ...
    │   └── <branch name>
    ├── gitletignore                                          <==== Where CWD files to ignore are stored (a file)
    └── HEAD                                                  <==== Where the HEAD reference is stored (a file)
```

# Notes

## Hashing
A SHA-1 hash will be created for the following two objects:

1. Blob
2. Commit

### Blob object
To distinguish different files, including different versions of files, we will get a SHA-1 hash on the contents. But
there may be times when the contents are the same (file is renamed, or separate files with different names  have the
same contents). In these instances, we will still want to produce the same SHA-1 hash. Therefore, filename should not
play a factor in calculating the hash.

### Commit object
The SHA-1 hash for a Commit object needs to include all metadata and references. Therefore, the SHA-1 hash will be the
result of the log message, timestamp, mapping of file names to blob references (tree), a parent reference, and (for 
merges) a second parent reference.

## Checkpoint commands
### init

Creates a new Gitlet version-control system in the current directory. This system will automatically start with one
commit: a commit that contains no files and has the commit message `initial commit` (just like that, with no
punctuation). It will have a single branch: `master`, which initially points to this initial commit, and `master` will
be the current branch. The timestamp for this initial commit will be 00:00:00 UTC, Thursday, 1 January 1970.

#### File Structure

```
CWD
└── .gitlet
    ├── blobs (empty)
    ├── commits
    │   └── <initial commit's SHA-1 hash>
    ├─── refs
    │   └── master
    ├── gitletignore
    └── HEAD
```

### add

Adds a copy of the file as it currently exists to the staging area. Staging an already-staged file overwrites the
previous entry in the staging area with the new contents. The staging area should be somewhere in `.gitlet`. If the
current working version of the file is identical to the version in the current commit, do not stage it to be added, and
remove it from the staging area if it is already there

### commit

By default a commit has the same file contents as its parent. Files staged for addition and removal are the updates to
the commit. Of course, the date (and likely the mesage) will also different from the parent.

### checkout -- [file name]

Takes the version of the file as it exists in the head commit and puts it in the working directory, overwriting the
version of the file that’s already there if there is one. The new version of the file is not staged.

### checkout [commit id] -- [file name]

Takes the version of the file as it exists in the commit with the given id, and puts it in the working directory,
overwriting the version of the file that’s already there if there is one. The new version of the file is not staged.

### log

Starting at the current head commit, display information about each commit backwards along the commit tree until the
initial commit, following the first parent commit links, ignoring any second parents found in merge commits. For every
node in this history, the information it should display is the commit id, the time the commit was made, and the commit
message.


## Remaining Commands
### status

Displays what branches currently exist, and marks the current branch with a *. Also displays what files have been staged
for addition or removal.

### rm

Unstage the file if it is currently staged for addition. If the file is tracked in the current commit, stage it for
removal and remove the file from the working directory if the user has not already done so (do not remove it unless it
is tracked in the current commit).

### global-log

Like log, except displays information about all commits ever made.

### find

Prints out the ids of all commits that have the given commit message, one per line. If there are multiple such commits,
it prints the ids out on separate lines.

### branch

Creates a new branch with the given name, and points it at the current head commit.

### checkout [branch name]

Takes all files in the commit at the head of the given branch, and puts them in the working directory, overwriting the
versions of the files that are already there if they exist. Also, at the end of this command, the given branch will now
be considered the current branch (HEAD). Any files that are tracked in the current branch but are not present in the
checked-out branch are deleted. The staging area is cleared, unless the checked-out branch is the current branch.

### rm-branch

Deletes the branch with the given name. This only means to delete the pointer associated with the branch.

### reset

Checks out all the files tracked by the given commit. Removes tracked files that are not present in that commit. Also
moves the current branch’s head to that commit node.

### merge