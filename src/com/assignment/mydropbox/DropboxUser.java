package com.assignment.mydropbox;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;

@PersistenceCapable
public class DropboxUser {

	/* -----------------------------*/
	/*			Fields				*/
	/* -----------------------------*/
	
	/* Unique id for identification */
	@PrimaryKey
	@Persistent
	private Key id;
	
	/* The key to the users current directory */
	@Persistent
	private Key currentDir;
	
	/* -----------------------------*/
	/*			Methods				*/
	/* -----------------------------*/

	/**
	 * Constructor creating a user with an id
	 * @param id Key object as id
	 */
	public DropboxUser(Key id) {
		this.id = id;
	}
	
	/**
	 * Getter for this users id
	 * @return Key object of id
	 */
	public Key getId() {
		return this.id;
	}
	
	/**
	 * Set the directory this user is currently in.
	 * @param dir Key to the new directory
	 */
	public void setCurrentDir(Key dir) {
		this.currentDir = dir;
	}
	
	/**
	 * Gets the Key of this users current directory
	 * @return Key object for current directory
	 */
	public Key getCurrentDir() {
		return this.currentDir;
	}
	
	
	
}
