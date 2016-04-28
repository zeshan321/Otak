function serverStatus(type) {
    span = document.getElementById("server-status");
    switch (type) {
        case "sync":
            span.className = "label label-warning";
            span.textContent = "Syncing..."
            break;

        case "connecting":
            span.className = "label label-warning";
            span.textContent = "Connecting..."
            break;

        case "online":
            span.className = "label label-success";
            span.textContent = "Online"
            break;

        case "offline":
            span.className = "label label-danger";
            span.textContent = "Offline"
            break;
    }
}

function removeItem(href) {
	var item = $("div[href=\"" + href + "\"]");
	item.remove();
}

function clearItems() {
	var myNode = document.getElementById("contents");
	while (myNode.firstChild) {
		myNode.removeChild(myNode.firstChild);
	}
}

function addItem(loc, name, type) {
    switch (type) {
        case "folder":
            $("#contents").append("<div id=\"row-files\" class=\"icon\" href=\"" + loc + "\"><img href=\"" + loc + "\" src=\"images\/" + "folder-icon" + ".svg\"><\/img><br><span class=\"File-labels\"> " + name + "<\/span><\/div>");
            break;

        case "png":
            $("#contents").append("<div id=\"row-files\" class=\"icon\" href=\"" + loc + "\"><img href=\"" + loc + "\" src=\"images\/" + "photo-icon" + ".svg\"><\/img><br><span class=\"File-labels\"> " + name + "<\/span><\/div>");
            break;
        case "gif":
            $("#contents").append("<div id=\"row-files\" class=\"icon\" href=\"" + loc + "\"><img href=\"" + loc + "\" src=\"images\/" + "photo-icon" + ".svg\"><\/img><br><span class=\"File-labels\"> " + name + "<\/span><\/div>");
            break;
        case "jpeg":
            $("#contents").append("<div id=\"row-files\" class=\"icon\" href=\"" + loc + "\"><img href=\"" + loc + "\" src=\"images\/" + "photo-icon" + ".svg\"><\/img><br><span class=\"File-labels\"> " + name + "<\/span><\/div>");
            break;
        case "jpg":
            $("#contents").append("<div id=\"row-files\" class=\"icon\" href=\"" + loc + "\"><img href=\"" + loc + "\" src=\"images\/" + "photo-icon" + ".svg\"><\/img><br><span class=\"File-labels\"> " + name + "<\/span><\/div>");
            break;
        case "tiff":
            $("#contents").append("<div id=\"row-files\" class=\"icon\" href=\"" + loc + "\"><img href=\"" + loc + "\" src=\"images\/" + "photo-icon" + ".svg\"><\/img><br><span class=\"File-labels\"> " + name + "<\/span><\/div>");
            break;
        case "zip":
            $("#contents").append("<div id=\"row-files\" class=\"icon\" href=\"" + loc + "\"><img href=\"" + loc + "\" src=\"images\/" + "zip-icon" + ".svg\"><\/img><br><span class=\"File-labels\"> " + name + "<\/span><\/div>");
            break;
        case "tar":
            $("#contents").append("<div id=\"row-files\" class=\"icon\" href=\"" + loc + "\"><img href=\"" + loc + "\" src=\"images\/" + "zip-icon" + ".svg\"><\/img><br><span class=\"File-labels\"> " + name + "<\/span><\/div>");
            break;
        case "mp4":
            $("#contents").append("<div id=\"row-files\" class=\"icon\" href=\"" + loc + "\"><img href=\"" + loc + "\" src=\"images\/" + "video-icon" + ".svg\"><\/img><br><span class=\"File-labels\"> " + name + "<\/span><\/div>");
            break;
        case "avi":
            $("#contents").append("<div id=\"row-files\" class=\"icon\" href=\"" + loc + "\"><img href=\"" + loc + "\" src=\"images\/" + "video-icon" + ".svg\"><\/img><br><span class=\"File-labels\"> " + name + "<\/span><\/div>");
            break;
        case "mpg":
            $("#contents").append("<div id=\"row-files\" class=\"icon\" href=\"" + loc + "\"><img href=\"" + loc + "\" src=\"images\/" + "video-icon" + ".svg\"><\/img><br><span class=\"File-labels\"> " + name + "<\/span><\/div>");
            break;
        case "mov":
            $("#contents").append("<div id=\"row-files\" class=\"icon\" href=\"" + loc + "\"><img href=\"" + loc + "\" src=\"images\/" + "video-icon" + ".svg\"><\/img><br><span class=\"File-labels\"> " + name + "<\/span><\/div>");
            break;
        case "mp3":
            $("#contents").append("<div id=\"row-files\" class=\"icon\" href=\"" + loc + "\"><img href=\"" + loc + "\" src=\"images\/" + "music-icon" + ".svg\"><\/img><br><span class=\"File-labels\"> " + name + "<\/span><\/div>");
            break;
        case "jar":
            $("#contents").append("<div id=\"row-files\" class=\"icon\" href=\"" + loc + "\"><img href=\"" + loc + "\" src=\"images\/" + "java-icon" + ".svg\"><\/img><br><span class=\"File-labels\"> " + name + "<\/span><\/div>");
            break;
        case "ai":
            $("#contents").append("<div id=\"row-files\" class=\"icon\" href=\"" + loc + "\"><img href=\"" + loc + "\" src=\"images\/" + "illustrator-icon" + ".svg\"><\/img><br><span class=\"File-labels\"> " + name + "<\/span><\/div>");
            break;
        case "psd":
            $("#contents").append("<div id=\"row-files\" class=\"icon\" href=\"" + loc + "\"><img href=\"" + loc + "\" src=\"images\/" + "photoshop-icon" + ".svg\"><\/img><br><span class=\"File-labels\"> " + name + "<\/span><\/div>");
            break;
        case ".txt":
            $("#contents").append("<div id=\"row-files\" class=\"icon\" href=\"" + loc + "\"><img href=\"" + loc + "\" src=\"images\/" + "word-icon" + ".svg\"><\/img><br><span class=\"File-labels\"> " + name + "<\/span><\/div>");
            break;
        case "doc":
            $("#contents").append("<div id=\"row-files\" class=\"icon\" href=\"" + loc + "\"><img href=\"" + loc + "\" src=\"images\/" + "word-icon" + ".svg\"><\/img><br><span class=\"File-labels\"> " + name + "<\/span><\/div>");
            break;
        case "docs":
            $("#contents").append("<div id=\"row-files\" class=\"icon\" href=\"" + loc + "\"><img href=\"" + loc + "\" src=\"images\/" + "word-icon" + ".svg\"><\/img><br><span class=\"File-labels\"> " + name + "<\/span><\/div>");
            break;
        case "pages":
            $("#contents").append("<div id=\"row-files\" class=\"icon\" href=\"" + loc + "\"><img href=\"" + loc + "\" src=\"images\/" + "word-icon" + ".svg\"><\/img><br><span class=\"File-labels\"> " + name + "<\/span><\/div>");
            break;
        case "xls":
            $("#contents").append("<div id=\"row-files\" class=\"icon\" href=\"" + loc + "\"><img href=\"" + loc + "\" src=\"images\/" + "excel-icon" + ".svg\"><\/img><br><span class=\"File-labels\"> " + name + "<\/span><\/div>");
            break;
        case "numbers":
            $("#contents").append("<div id=\"row-files\" class=\"icon\" href=\"" + loc + "\"><img href=\"" + loc + "\" src=\"images\/" + "excel-icon" + ".svg\"><\/img><br><span class=\"File-labels\"> " + name + "<\/span><\/div>");
            break;
        case "xlsx":
            $("#contents").append("<div id=\"row-files\" class=\"icon\" href=\"" + loc + "\"><img href=\"" + loc + "\" src=\"images\/" + "excel-icon" + ".svg\"><\/img><br><span class=\"File-labels\"> " + name + "<\/span><\/div>");
            break;
        case "csv":
            $("#contents").append("<div id=\"row-files\" class=\"icon\" href=\"" + loc + "\"><img href=\"" + loc + "\" src=\"images\/" + "excel-icon" + ".svg\"><\/img><br><span class=\"File-labels\"> " + name + "<\/span><\/div>");
            break;
        case "ppt":
            $("#contents").append("<div id=\"row-files\" class=\"icon\" href=\"" + loc + "\"><img href=\"" + loc + "\" src=\"images\/" + "presentation-icon" + ".svg\"><\/img><br><span class=\"File-labels\"> " + name + "<\/span><\/div>");
            break;
        case "keynote":
            $("#contents").append("<div id=\"row-files\" class=\"icon\" href=\"" + loc + "\"><img href=\"" + loc + "\" src=\"images\/" + "presentation-icon" + ".svg\"><\/img><br><span class=\"File-labels\"> " + name + "<\/span><\/div>");
            break;
        case "pptm":
            $("#contents").append("<div id=\"row-files\" class=\"icon\" href=\"" + loc + "\"><img href=\"" + loc + "\" src=\"images\/" + "presentation-icon" + ".svg\"><\/img><br><span class=\"File-labels\"> " + name + "<\/span><\/div>");
            break;

        default:
            $("#contents").append("<div id=\"row-files\" class=\"icon\" href=\"" + loc + "\"><img href=\"" + loc + "\" src=\"images\/" + "file-icon" + ".svg\"><\/img><br><span class=\"File-labels\"> " + name + "<\/span><\/div>");
    }
}
