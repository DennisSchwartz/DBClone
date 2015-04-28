package com.assignment.mydropbox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

@SuppressWarnings("serial")
public class FilesServlet extends HttpServlet {

	/**
	 * The doPost method will be invoked any time there is a POST request to /files
	 */
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		/* 
		 * First:
		 *  Get user and current directory (same as in RootServlet):  
		 */

		PersistenceManager pm = PMF.get().getPersistenceManager();
		UserService us = UserServiceFactory.getUserService();
		User u = us.getCurrentUser();
		Key user_key = KeyFactory.createKey("DropboxUser", u.getUserId());
		DropboxUser user = pm.getObjectById(DropboxUser.class, user_key);
		Key dirKey = user.getCurrentDir();
		/* Check for path */
		DropboxDirectory dir = null;
		try {
			/* Get current path */
			dir = pm.getObjectById(DropboxDirectory.class, dirKey);
		} catch(Exception e) {
			System.out.println("Directory not found!");
		}

		/*
		 * Then:
		 * 	Get the uploaded files (multipart/form-data) from the request object directed at this Servlet 
		 */

		Map<String, List<BlobKey>> files_sent = BlobstoreServiceFactory.getBlobstoreService().getUploads(req);
		/* Get the BlobKey for the uploaded file */
		try {
			/* If not file was chosen, this will cause an Exception */
			BlobKey b = files_sent.get("file").get(0);
			BlobInfo info = new BlobInfoFactory().loadBlobInfo(b);
			/* Else throw an exception if the file is empty */
			if (info.getSize() == 0) {
				throw new Exception();
			}
			/* Get the filename of the uploaded file from the BlobInfo */
			String name = info.getFilename();
			/* Create new {@DropboxFile} object from the name, BlobKey and current directory */
			DropboxFile f = new DropboxFile(name, b, dir);
			/* Add the new file to the current directories file list */
			dir.addFile(f);
			/* Make the file persistent */
			pm.makePersistent(f);
			pm.close();

			/* Return to the root page */
			resp.sendRedirect("/");
		} catch (Exception e) {
			/* If there is no sent file -> Return to the root page */
			resp.sendRedirect("/");
		}
	}

	/**
	 * The doGet method will be invoked any time there is a GET request to /files
	 */
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		/* 
		 * First:
		 *  Get user and current directory (same as in RootServlet):  
		 */

		PersistenceManager pm = PMF.get().getPersistenceManager();
		UserService us = UserServiceFactory.getUserService();
		User u = us.getCurrentUser();
		Key user_key = KeyFactory.createKey("DropboxUser", u.getUserId());
		DropboxUser user = pm.getObjectById(DropboxUser.class, user_key);
		Key dirKey = user.getCurrentDir();
		/* Check for path */
		DropboxDirectory dir = null;
		try {
			/* Get current path */
			dir = pm.getObjectById(DropboxDirectory.class, dirKey);
		} catch(Exception e) {
			System.out.println("Directory not found!");
		}

		/*
		 * Then:
		 * 	Check the kind of action the user chose. (Which button has been pressed)
		 */

		String action = req.getParameter("action");
		if (action.equals("serve")) {

			/*
			 * If the user pressed the download button
			 *  -> Serve file to user 
			 */

			serveFile(req, resp);

		} else if (action.equals("delete")) {

			/*
			 * If the user pressed the delete button
			 *  -> Delete file from DropboxDirectory, PersistenceManager and BlobStore
			 */

			deleteFile(req, resp, user, dir, pm);

		}

		/* Finally close the PersistenceManager */
		pm.close();

	}

	/**
	 * Private function to serve a File
	 * @param req Original request object
	 * @param resp Original response object
	 * @throws IOException
	 */
	private void serveFile(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		/* Get key of file to serve */
		BlobKey blobKey = new BlobKey(req.getParameter("file"));
		/* Get original filename and extension */
		BlobInfoFactory blobInfo = new BlobInfoFactory();
		String name = blobInfo.loadBlobInfo(blobKey).getFilename();
		resp.setContentType("application/x-download");
		resp.setHeader("Content-Disposition", "attachment; filename=" + name);
		/* Serve file */
		BlobstoreServiceFactory.getBlobstoreService().serve(blobKey, resp); 
	}


	/**
	 * Private method to delete a file from all Stores, given its Blobkey Keystring
	 * @param req
	 * @param resp
	 * @param user
	 * @param dir
	 * @param pm
	 * @throws IOException
	 */
	private void deleteFile(HttpServletRequest req, HttpServletResponse resp, DropboxUser user, DropboxDirectory dir, PersistenceManager pm) throws IOException {
		/* Get key of file to delete */
		BlobKey blobKey = new BlobKey(req.getParameter("file"));
		ArrayList<DropboxFile> files = (ArrayList<DropboxFile>) dir.files();
		DropboxFile file = null;
		/* Find the correct file in the current dir by its BlobKey */
		for (DropboxFile f: files) {
			if (f.getBlobKey().equals(blobKey)) {
				dir.deleteFile(f);
				file = f;
			}
		}
		/* Delete from PersistenceManager */
		pm.deletePersistent(file);
		/* Delete from BlobStore */
		BlobstoreServiceFactory.getBlobstoreService().delete(blobKey);
		/* Redirect back to root */
		resp.sendRedirect("/");
	}



}
