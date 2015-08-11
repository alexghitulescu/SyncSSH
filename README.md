# SyncSSH
Simple Java tool that can be used to download and upload files from a linux server. It will sync only changed files after first run.

It needs a SyncSSH.ini file to work. See the one in example.

After running it will create a SyncSSH.log file, where all logs are store. If exceptions appear, they will be written to a SyncSSH.err file. These two files are going to be created next to the SyncSHH.ini file.

Usage:
	- encrypt password for ini file
		"java -jar SyncSSH.jar pass <your password>"
	- download
		"java -jar SyncSSH.jar download <path to SyncSSH.ini file, use . for current directory>"
	-upload
		"java -jar SyncSSH.jar upload <path to SyncSSH.ini file>"