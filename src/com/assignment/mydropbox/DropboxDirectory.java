package com.assignment.mydropbox;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;


@PersistenceCapable
public class DropboxDirectory {

	/* -----------------------------*/
	/*			Fields				*/
	/* -----------------------------*/

	/* Persistent id, auto generated (or maybe not?) */
	@PrimaryKey
	@Persistent //(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key id;

	/* Name of the directory */
	@Persistent
	private String name;

	/* List of (Keys of) sub directories */
	@Persistent
	private List<Key> subdirs;

	/* List of files contained in this directory
	 * Enforces parent-child relationship */
	@Persistent(mappedBy="parent")
	private List<DropboxFile> files;

	/* Store path to this dir */
	@Persistent
	private List<String> path;

	/* Directory key of the parent directory */
	@Persistent
	private Key parent;

	/* -----------------------------*/
	/*			Methods				*/
	/* -----------------------------*/

	/**
	 * Constructor method instantiating a DropboxDirectory object
	 * @param id The id as a Key object
	 * @param name Directory name as string ending with a slash '/'
	 */
	public DropboxDirectory(Key id, String name) {
		this.id = id;
		this.name = checkName(name); // Check for trailing "/"
		this.path = new ArrayList<String>();
		this.files = new ArrayList<DropboxFile>();
		this.subdirs = new ArrayList<Key>();
	}

	/**
	 * Method to add a sub directory to the current directory
	 * @param name Name of the sub directory to be created
	 */
	public void addSubDir(Key id) {
		this.subdirs.add(id);
	}

	/**
	 * Checks if directory name already exists within this directory
	 * @param name Directory name to be checked
	 * @return Boolean value. Returns true if directory with name 'name' already exists in this folder.
	 */
	public Boolean subdirExists(Key id) {
		/* iterate through list of sub directories */ 
		for (Key k: this.subdirs) {
			if (k.equals(id)) {
				return true; 
			}
		}
		return false;
	}

	/**
	 * Check if this directory is empty
	 * @return True if there are no DropboxFile objects or sub directories in this folder
	 */
	public Boolean isEmpty() {
		return (this.files.isEmpty() && this.subdirs.isEmpty());
	}

	/**
	 * Adds a file to the file list of this folder
	 * @param file DropboxFile object
	 */
	public void addFile(DropboxFile file) {
		this.files.add(file);
	}

	/**
	 * Deletes a file from the file list
	 * @param file DropboxFile object to be deleted
	 */
	public void deleteFile(DropboxFile file) {
		if (this.files.contains(file)) {
			this.files.remove(file);
		}
	}

	/**
	 * Deletes a sub directory
	 * @param name Directory to be deleted
	 * @throws Exception If directory name does not exist
	 */
	public void deleteSubDir(Key id) throws Exception {
		if (this.subdirExists(id)) {
			this.subdirs.remove(id);
		} else {
			throw new Exception("This directory does not exist!");
		}
	}

	/**
	 * Returns the sub directories of this folder 
	 * @return List of directory names belonging to this folder
	 */
	public List<Key> directories() {
		if (this.subdirs == null) {
			return new ArrayList<Key>();
		} else {
			return this.subdirs;
		}
	}

	/**
	 * Returns list of files in this directories
	 * @return List of DropboxFile objects
	 */
	public List<DropboxFile> files() {
		if (this.files == null) {
			return new ArrayList<DropboxFile>();
		} else {
			return this.files;
		}
	}
	
	/**
	 * Get the name of this directory
	 * @return Name of this dir as String
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Adds a new directory name to the path
	 * @param path String 
	 */
	//TODO: This doesn't work like this
	public void addPath(String path) {
		if (this.path == null) {
			this.path = new ArrayList<String>();
		}
		this.path.add(checkName(path));
	}
	
	/**
	 * Set path to this directory
	 * @param path List of Strings
	 */
	public void setPath(List<String> path) {
		this.path = path;
	}

	/**
	 * Returns the path to this directory as a single String
	 * @return String
	 */
	public String getPathAsString() {
		String res = "";
		for (String s: this.path) {
			res += s;
		}
		return res;
	}
	
	/**
	 * Returns the path to this directory as a list of strings
	 * @return List<String> object
	 */
	public List<String> getPath() {
		return this.path;
	}
	
	/**
	 * Returns the Key object for this directory
	 * @return Key object
	 */

	public Key getKey() {
		return this.id;
	}

	/**
	 * Set the parent directory i.e. set the folder containing this directory
	 * @param id Key object of the new parent
	 */
	public void setParent(Key id) {
		this.parent = id;
	}

	/**
	 * Get the key object of the parent directory
	 * @return Key object
	 */
	public Key getParent() {
		return this.parent;
	}

	
	/**
	 * Checks directory name for trailing "/". And adds it when appropriate
	 * @param name String representing the name for a directory
	 * @return name String with trailing "/"
	 */
	private String checkName(String name) {
		if (name.isEmpty()) return "/";
		if (!name.trim().substring(name.length() - 1).equals("/")) {
			name = name + "/";
		}
		return name;
	}

}
