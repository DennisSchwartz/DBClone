<%@ page
	import="com.google.appengine.api.blobstore.BlobstoreServiceFactory"%>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreService"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Dropbox</title>
<link rel="stylesheet" href="style.css">
<link rel="stylesheet" href="bootstrap.min.css">
<link href="//netdna.bootstrapcdn.com/bootstrap/3.0.0/css/bootstrap-glyphicons.css" rel="stylesheet">
</head>
<body>
	<div class="container">
		<c:choose>
			<c:when test="${user != null}">
				<div class="row">
					<div class="col-lg-4">
						<p class="lead">Welcome ${user.email}</p>
					</div>
					<div class="col-lg-4">
						<p class="lead"><span class="glyphicon glyphicon-folder-open"></span> ${ current_path }</p>
					</div>
					<div class="col-lg-2 pull-right">
						<a href="${logout_url}" class="btn btn-info">Sign out</a>
					</div>
				</div>
				<div class="row form-inline">
					<div class="col-md-3">
					<div class="form-group pull-left">
						<form class="inline" id="createDir" action="/directory" method="post">
						<label class="control-label" for="dirName">Create Folder</label>
							<div class="row">
								<input type="text" class="form-control" name="dirName" value="New Folder"> 
								<input type="hidden" name="action" value="add"> 
								<button form="createDir" type="submit" class="btn btn-small">
									<span class="glyphicon glyphicon-plus"></span>
								</button>
								<!--  <input type="submit" class="btn btn-small" value="Create"> -->
								</div>
						</form>
					</div>
					</div>
					<div class="inline">
					<div class="form-group">
					<label class="control-label" for="upload">Upload File</label>
					<form enctype="multipart/form-data" class="form" method="post" id="upload" action="${ upload_url }">
						<div class="row">
							<span class="btn btn-default btn-file"> Browse <input type="file" name="file"></span>
							<button form="upload" type="submit" class="btn btn-small">
								<span class="glyphicon glyphicon-cloud-upload"></span>
							</button>
						</div>
					</form>
					</div>
					</div>
				</div>

				<ul class="row list-group">	
					<c:if test="${ parent_path != null }">
						<li class="list-group-item">
							<div class="row-fluid">
							<span class="glyphicon glyphicon-arrow-up"></span>
							<button type="submit" form="parent" class="btn btn-xs btn-link">
									<span class="text-primary">...</span>
							</button>
							</div>
							<form action="/directory" id="parent" method="post">
								<input type="hidden" name="dirName" value="${ parent_path }">
								<input type="hidden" name="action" value="change"> 
							</form>
						</li>
					</c:if>	
					<c:if test="${ empty blobs && empty subdirs && parent_path == null }">
						<li class="list-group-item">
							<div class="row-fluid">
								<span class="text-muted">There is nothing here yet.</span>
							</div>
						</li>
					</c:if>				
					<c:forEach items="${ subdirs }" var="dir" begin="0"	varStatus="loop">
						<li class="list-group-item">
							<div class="row-fluid">
									<span class="glyphicon glyphicon-folder-close"></span>
									<button form="change" type="submit" class="btn btn-xs btn-link">
										<span class="text-primary"><c:out value="${ dir }" /></span>
									</button>	
									<button form="delete" type="submit" class="btn btn-xs btn-danger pull-right">
										<span class="glyphicon glyphicon-trash"></span>
									</button>
							</div>
							<form id="delete" action="/directory" method="post">
									<input type="hidden" name="dirName" value="${ dir }"> 
									<input type="hidden" name="action" value="delete"> 
								</form>
								<form id="change" action="/directory" method="post">
									<input type="hidden" name="dirName" value="${ dir }"> 
									<input type="hidden" name="action" value="change"> 
									<!-- <input type="submit" class="btn btn-small" value="change"> -->
								</form>
						</li>
					</c:forEach>
					<c:forEach items="${ blobs }" var="file" begin="0" varStatus="loop">
						<li class="list-group-item">	
							<div class="row-fluid">	
								<span class="glyphicon glyphicon-file"></span>
								<button form="serve" type="submit" class="btn btn-xs btn-link">		
									<c:out value="${ files[loop.index].name }" />
								</button>
								<button form="delFile" type="submit" class="btn btn-xs btn-danger pull-right">
										<span class="glyphicon glyphicon-trash"></span>
								</button>
							
							
								<form action="/files" id="serve" method="get">
									<input type="hidden" name="file" value="${ file }"> 
									<input type="hidden" name="action" value="serve"> 
								</form>
								<form action="/files" id="delFile" method="get">
									<input type="hidden" name="file" value="${ file }"> 
									<input type="hidden" name="action" value="delete"> 
								</form>
							</div>
						</li>
					</c:forEach>
				</ul>
			</c:when>
			<c:otherwise>
				<p>
					Welcome! <a href="${login_url}">Sign in or register</a>
				</p>
			</c:otherwise>
		</c:choose>
	</div>
</body>
</html>