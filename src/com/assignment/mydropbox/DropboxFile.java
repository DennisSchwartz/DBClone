package com.assignment.mydropbox;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.Key;

@PersistenceCapable
public class DropboxFile {
	
	/* -----------------------------*/
	/*			Fields				*/
	/* -----------------------------*/

	// Key for unique file identification
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key id;
	
	// Name of this file
	@Persistent
	private String name;
	
	// Parent directory
	@Persistent
	private DropboxDirectory parent;
	
	// Owner
	@Persistent
	private DropboxUser owner;
	
	// Blob key for access in Blobstore
	@Persistent
	private BlobKey blobKey;
	
	/* -----------------------------*/
	/*			Methods				*/
	/* -----------------------------*/
	
	/**
	 * Constructor method instantiating a DropboxFile object
	 * @param name Name of the file
	 * @param blobKey BlobKey for access in BlobStore
	 * @param owner	User which owns the file
	 */
	public DropboxFile(String name, BlobKey blobKey, DropboxDirectory parent) {
		this.name = name;
		this.blobKey = blobKey;
		this.parent = parent;
	}
	
	/**
	 * Returns the name of the File
	 * @return String representing the file name
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Returns the BlobKey of this file instance for access in BlobStore
	 * @return BlobKey object of this file 
	 */
	public BlobKey getBlobKey() {
		return this.blobKey;
	}
	
}
