package com.assignment.mydropbox;

import java.io.IOException;
import java.util.ArrayList;

import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

/**
 * This Servlet handles the changing, creation and deletion of directories.
 */
@SuppressWarnings("serial")
public class DirectoryServlet extends HttpServlet {

	@SuppressWarnings("unchecked")
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		/* 
		 * First:
		 *  Get user and current directory (same as in RootServlet):
		 *  BTW: It would make sense to create a DropboxUserService and DropboxDirectoryService for this task.
		 *  Then you could just call DBS.getUser() or so instead of implementing this in every Servlet.  
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
		 * 	Get the information about the directory change from the request object
		 */

		/* What's the name of the new directory? */
		String target = checkName(req.getParameter("dirName"));
		/* Build directory path */
		ArrayList<String> path = (ArrayList<String>) dir.getPath();
		ArrayList<String> newPath = (ArrayList<String>) path.clone();
		newPath.add(target);
		/* The key for the target directory is generated using its path */
		Key newDirKey = KeyFactory.createKey("DropboxDirectory", dir.getPathAsString().concat(target).concat(u.getUserId()));		
	
		
		/*
		 * Then:
		 * 	Check the kind of action that occurred. (Which button has been pressed)
		 */
		
		String action = req.getParameter("action");
		if (action.equals("add")) {
			/* Add a new subdir */
			if (!dir.subdirExists(newDirKey)){
				// Create new Directory
				newDirKey = KeyFactory.createKey("DropboxDirectory", dir.getPathAsString().concat(target).concat(u.getUserId()));
				DropboxDirectory dir2 = new DropboxDirectory(newDirKey, target);
				dir.addSubDir(dir2.getKey());
				dir2.setParent(dir.getKey());
				dir2.setPath(newPath);
				System.out.println(dir2.getName() + " was created!");
				pm.makePersistent(dir2);
			} else {
				// TODO Send notification to RootServlet
			}
		} else if (action.equals("delete")) {
			if (dir.subdirExists(newDirKey)) {
				try {
					DropboxDirectory delDir = pm.getObjectById(DropboxDirectory.class, newDirKey);
					if (!delDir.isEmpty()) throw new Exception("This directory is not empty!");
					dir.deleteSubDir(newDirKey);
					pm.deletePersistent(delDir);
				} catch (Exception e) {
					// TODO Attach reason for not deleting dir
					System.err.println("Error: \n" + e.getMessage() + "\n");
					e.printStackTrace();
					resp.sendRedirect("/");
				}
			}
		} else if (action.equals("change")) {
			if (dir.subdirExists(newDirKey)) {
				user.setCurrentDir(newDirKey);
				System.out.println("Changed to: " + user.getCurrentDir().getName());
			} else {
				Key parent = dir.getParent();
				user.setCurrentDir(parent);
				System.out.println("Changed to ../");
			}
		}


		pm.close();
		
		resp.sendRedirect("/");
	}

	// If name doesn't end in '/', add it.
	private String checkName(String name) {
		if (name.isEmpty()) return "/";
		if (!name.trim().substring(name.length() - 1).equals("/")) {
			name = name + "/";
		}
		return name;
	}
	
}
