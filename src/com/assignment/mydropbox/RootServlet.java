package com.assignment.mydropbox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;


/**
 * 
 * This is the root servlet. Loading the / page of the server in your browser will invoke a HTTP Get request to this servlet. 
 * It will form a response object and forward it to the root.jsp file.
 *
 */
@SuppressWarnings("serial")
public class RootServlet extends HttpServlet {

	/**
	 * The doGet method will be invoked any time there is a GET request to /
	 */
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {

		/* set content type of the response to HTML */
		resp.setContentType("text/html");

		/* 
		 * 	Check if user has logged in before. If yes, it will be stored in the UserService of GAE.
		 *  UserService will also create login/-out URLs. 
		 */
		
		UserService us = UserServiceFactory.getUserService();
		User u = us.getCurrentUser();
		String login_url = us.createLoginURL("/");
		String logout_url = us.createLogoutURL("/");

		/* 
		 * Attach the generated URLs and the user to the request object (which will be forwarded to .jsp)
		 * If there is no user yet, it will pass a 'null' object 
		 */
		
		req.setAttribute("user", u);
		req.setAttribute("login_url", login_url);
		req.setAttribute("logout_url", logout_url);

		/* Get the persistence manager */
		PersistenceManager pm = PMF.get().getPersistenceManager();


		/* 
		 * Check if the UserService returned a user.
		 * If it hasn't, forward to the root.jsp. 
		 * Since the u == null, it will display the login link and greeting. 
		 */
		
		if (u == null) {

			RequestDispatcher rd = req.getRequestDispatcher("/WEB-INF/root.jsp");
			rd.forward(req, resp);

		} else {

			/* Get or create current DropboxUser */
			DropboxUser user = getCurrentUser(u, pm);
			
			/* Get or create current DropboxDirectory */
			DropboxDirectory dir = getCurrentDir(user, pm, u);

			/* Last Step:
			 * 	Attach all the generated/retrieved information to the request object 
			 *  and forward it to the root.jsp file for display 
			 */

			/* Attach directory to request */
			req.setAttribute("dir", dir);
			req.setAttribute("current_dir", dir.getName());
			req.setAttribute("current_path", dir.getPathAsString());
			req.setAttribute("subdirs", getDirNames(dir.directories(), pm));
			/* If the current directory is root, don't set the parent path*/
			Key dirKey = KeyFactory.createKey("DropboxDirectory", u.getUserId());
			if (!dir.getKey().equals(dirKey)) {
				req.setAttribute("parent_path", dir.getParent());
			}

			/* Attach files to request */
			List<DropboxFile> files = dir.files();
			req.setAttribute("files", files);
			req.setAttribute("blobs", blobKeyStrings(files)); // Store blobs as their keystrings

			/* Set upload URL and forward request */
			String upload_url = BlobstoreServiceFactory.getBlobstoreService().createUploadUrl("/files");
			req.setAttribute("upload_url", upload_url);
			RequestDispatcher rd = req.getRequestDispatcher("/WEB-INF/root.jsp");
			rd.forward(req, resp);

		}

		/* Finally, close the PersistenceManager */
		pm.close();

	}
	
	/**
	 * Private function returning the current DropboxUser or instantiating a new one 
	 * @param u Current UserService user
	 * @param pm PersistenceManager
	 * @return Current or new DropboxUser
	 */
	private DropboxUser getCurrentUser(User u, PersistenceManager pm) {
		/* We need a DropboxUser but it's not set yet */
		DropboxUser user = null;
		/* If the UserService returned a valid user, load the directories and files for this user */
		/* Generate unique key for this DropboxUser from its UserService userId */
		Key user_key = KeyFactory.createKey("DropboxUser", u.getUserId());
		try {
			/* Get the stored DropboxUser from the persistence store by its user key */
			user = pm.getObjectById(DropboxUser.class, user_key);
		} catch(Exception e) {
			/* If there is no user with that key -> create new user */
			user = new DropboxUser(user_key);
			/* Save user in persistence store */
			pm.makePersistent(user);
		}
		return user;
	}

	/**
	 * Private function returning the current DropboxDirectory.
	 * If none, generates a new root directory
	 * @param user Current DropboxUser
	 * @param pm PersistenceManager
	 * @return Current directory of the given DropboxUser
	 */
	private DropboxDirectory getCurrentDir(DropboxUser user, PersistenceManager pm, User u) {
		/* Check if the user has stored a key for a current directory and get it.
		 * If not: Create root directory, set it as current directory */
		DropboxDirectory dir = null;
		try {
			Key k = user.getCurrentDir();
			dir = pm.getObjectById(DropboxDirectory.class, k);
		} catch(Exception e) {
			/* If no current directory available, create a new root and assign it to the user */
			Key dirKey = KeyFactory.createKey("DropboxDirectory", u.getUserId());
			dir = new DropboxDirectory(dirKey, "/");
			dir.addPath("/");
			user.setCurrentDir(dir.getKey());
			/* Save folder in persistence store */
			pm.makePersistent(dir);
		}
		return dir;
	}

	/**
	 * Private Function retrieving the directory names from a list of directory key objects
	 * @param keys Keys to the directories
	 * @param pm The current PeristenceManager
	 * @return List of names for the directories
	 */
	private List<String> getDirNames(List<Key> keys, PersistenceManager pm) {
		DropboxDirectory dir = null;
		ArrayList<String> res = new ArrayList<String>();
		for (Key k: keys) {
			dir = pm.getObjectById(DropboxDirectory.class, k);
			res.add(dir.getName());
		}
		return res;
	}


	/**
	 * Gets the blob key strings of a list of {@DropboxFile} objects
	 * @param files {@link DropboxFile} Objects
	 * @return List of strings
	 */
	private List<String> blobKeyStrings(List<DropboxFile> files) {
		List<String> blobs = new ArrayList<String>();
		for (DropboxFile f: files) {
			blobs.add(f.getBlobKey().getKeyString());
		}
		return blobs;
	}

}
