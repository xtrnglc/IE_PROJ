**Please move ie.jar into the IE_PROJ directory**

To run our program, cd into the IE_PROJ directory in this tar submission.

Then run the program through the command line. The program takes in the test file/folder path for the first argument and a flag '-s'denoting whether the user is passing in a single file or not. Presence of the flag means the system will look for a single file, an omitted flag means the user is passing in a folder of files.

In our case, we\'92ll be working with the blind data set. Therefore, the user will want to run in the command line something like so:

java -jar ie.jar

or for a single file, something like so:

java ./Driver data/test-set-docs/20020516.4232.maintext -s

Also, make sure the included stanford-core NLP JAR files are being linked when compiling the code.}
