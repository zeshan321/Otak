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


//EXAMPLE usage: addFileProgress('PLACEHOLDER.txt', 90);
function addFileProgress(file, progress) {
  var pbar = $("#pb" + file.split(".")[0]);
  var filename = file.split(".")[0];
  
  if(pbar.length != 0){ //update the progress bar
	if (progress != 100) {
		pbar.html("<progress value='" + progress + "' max='100'></progress>");
	} else {
		$("#tr" + filename).remove();
		$("#" + filename).remove();
		pbar.remove();
	}
  } else { //add the new progress bar
    $("#filesinsync").append("<tr id='tr" + filename + "'> \
      <td id='" + filename + "'>" + file + "</td> \
      <td id='pb" + filename + "' class='pbar'><progress value='" + progress + "' max='100'></progress></td> \
      </tr>");
  }
  
  // Update sync status
  var syncinfo = $("#syncinfo");
  var tablesize = $("#synctable").find('tr').length -1;
  
  console.log(tablesize);
  if (tablesize == 0) {
	  syncinfo.text("No downloads");
  } else {
	  syncinfo.text("Downloading: " + tablesize + " files");
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

String.prototype.truncate = function(n){
    return this.substr(0,n-1)+(this.length>n?'&hellip;':'');
};


function onRowClick(event) {
	var source = event.target.parentElement;

	if (event.which == 1) {
		file.onClick('left', source.getAttribute("loc"), source.getAttribute("name"), source.getAttribute("type"));
	}

	if (event.which == 3) {
		file.onClick('right', source.getAttribute("loc"), source.getAttribute("name"), source.getAttribute("type"));
	}
}


function addItem(loc, name, type) {
	sort = name.truncate(16);
    switch (type) {
        case "folder":
            $("#contents").append("<div id=\"row-files\" class=\"icon\" onClick=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" loc=\"" + loc + "\" name=\"" + name + "\" type=\"" + type + "\"><img src=\"images\/" + "folder-icon" + ".svg\"><\/img><br><span data-toggle=\"tooltip\" class=\"File-labels\" title=\"" + name + "\"> " + sort + "<\/span><\/div>");
            break;

        case "png":
            $("#contents").append("<div id=\"row-files\" class=\"icon\" onClick=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" loc=\"" + loc + "\" name=\"" + name + "\" type=\"" + type + "\"><img src=\"images\/" + "photo-icon" + ".svg\"><\/img><br><span data-toggle=\"tooltip\" class=\"File-labels\" title=\"" + name + "\"> " + sort + "<\/span><\/div>");
            break;
        case "gif":
            $("#contents").append("<div id=\"row-files\" class=\"icon\" onClick=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" loc=\"" + loc + "\" name=\"" + name + "\" type=\"" + type + "\"><img src=\"images\/" + "photo-icon" + ".svg\"><\/img><br><span data-toggle=\"tooltip\" class=\"File-labels\" title=\"" + name + "\"> " + sort + "<\/span><\/div>");
            break;
        case "jpeg":
            $("#contents").append("<div id=\"row-files\" class=\"icon\" onClick=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" loc=\"" + loc + "\" name=\"" + name + "\" type=\"" + type + "\"><img src=\"images\/" + "photo-icon" + ".svg\"><\/img><br><span data-toggle=\"tooltip\" class=\"File-labels\" title=\"" + name + "\"> " + sort + "<\/span><\/div>");
            break;
        case "jpg":
            $("#contents").append("<div id=\"row-files\" class=\"icon\" onClick=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" loc=\"" + loc + "\" name=\"" + name + "\" type=\"" + type + "\"><img src=\"images\/" + "photo-icon" + ".svg\"><\/img><br><span data-toggle=\"tooltip\" class=\"File-labels\" title=\"" + name + "\"> " + sort + "<\/span><\/div>");
            break;
        case "tiff":
            $("#contents").append("<div id=\"row-files\" class=\"icon\" onClick=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" loc=\"" + loc + "\" name=\"" + name + "\" type=\"" + type + "\"><img src=\"images\/" + "photo-icon" + ".svg\"><\/img><br><span data-toggle=\"tooltip\" class=\"File-labels\" title=\"" + name + "\"> " + sort + "<\/span><\/div>");
            break;
        case "zip":
            $("#contents").append("<div id=\"row-files\" class=\"icon\" onClick=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" loc=\"" + loc + "\" name=\"" + name + "\" type=\"" + type + "\"><img src=\"images\/" + "zip-icon" + ".svg\"><\/img><br><span data-toggle=\"tooltip\" class=\"File-labels\" title=\"" + name + "\"> " + sort + "<\/span><\/div>");
            break;
        case "tar":
            $("#contents").append("<div id=\"row-files\" class=\"icon\" onClick=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" loc=\"" + loc + "\" name=\"" + name + "\" type=\"" + type + "\"><img src=\"images\/" + "zip-icon" + ".svg\"><\/img><br><span data-toggle=\"tooltip\" class=\"File-labels\" title=\"" + name + "\"> " + sort + "<\/span><\/div>");
            break;
        case "mp4":
            $("#contents").append("<div id=\"row-files\" class=\"icon\" onClick=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" loc=\"" + loc + "\" name=\"" + name + "\" type=\"" + type + "\"><img src=\"images\/" + "video-icon" + ".svg\"><\/img><br><span data-toggle=\"tooltip\" class=\"File-labels\" title=\"" + name + "\"> " + sort + "<\/span><\/div>");
            break;
        case "avi":
            $("#contents").append("<div id=\"row-files\" class=\"icon\" onClick=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" loc=\"" + loc + "\" name=\"" + name + "\" type=\"" + type + "\"><img src=\"images\/" + "video-icon" + ".svg\"><\/img><br><span data-toggle=\"tooltip\" class=\"File-labels\" title=\"" + name + "\"> " + sort + "<\/span><\/div>");
            break;
        case "mpg":
            $("#contents").append("<div id=\"row-files\" class=\"icon\" onClick=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" loc=\"" + loc + "\" name=\"" + name + "\" type=\"" + type + "\"><img src=\"images\/" + "video-icon" + ".svg\"><\/img><br><span data-toggle=\"tooltip\" class=\"File-labels\" title=\"" + name + "\"> " + sort + "<\/span><\/div>");
            break;
        case "mov":
            $("#contents").append("<div id=\"row-files\" class=\"icon\" onClick=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" loc=\"" + loc + "\" name=\"" + name + "\" type=\"" + type + "\"><img src=\"images\/" + "video-icon" + ".svg\"><\/img><br><span data-toggle=\"tooltip\" class=\"File-labels\" title=\"" + name + "\"> " + sort + "<\/span><\/div>");
            break;
        case "mp3":
            $("#contents").append("<div id=\"row-files\" class=\"icon\" onClick=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" loc=\"" + loc + "\" name=\"" + name + "\" type=\"" + type + "\"><img src=\"images\/" + "music-icon" + ".svg\"><\/img><br><span data-toggle=\"tooltip\" class=\"File-labels\" title=\"" + name + "\"> " + sort + "<\/span><\/div>");
            break;
        case "jar":
            $("#contents").append("<div id=\"row-files\" class=\"icon\" onClick=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" loc=\"" + loc + "\" name=\"" + name + "\" type=\"" + type + "\"><img src=\"images\/" + "java-icon" + ".svg\"><\/img><br><span data-toggle=\"tooltip\" class=\"File-labels\" title=\"" + name + "\"> " + sort + "<\/span><\/div>");
            break;
        case "ai":
            $("#contents").append("<div id=\"row-files\" class=\"icon\" onClick=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" loc=\"" + loc + "\" name=\"" + name + "\" type=\"" + type + "\"><img src=\"images\/" + "illustrator-icon" + ".svg\"><\/img><br><span data-toggle=\"tooltip\" class=\"File-labels\" title=\"" + name + "\"> " + sort + "<\/span><\/div>");
            break;
        case "psd":
            $("#contents").append("<div id=\"row-files\" class=\"icon\" onClick=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" loc=\"" + loc + "\" name=\"" + name + "\" type=\"" + type + "\"><img src=\"images\/" + "photoshop-icon" + ".svg\"><\/img><br><span data-toggle=\"tooltip\" class=\"File-labels\" title=\"" + name + "\"> " + sort + "<\/span><\/div>");
            break;
        case "txt":
            $("#contents").append("<div id=\"row-files\" class=\"icon\" onClick=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" loc=\"" + loc + "\" name=\"" + name + "\" type=\"" + type + "\"><img src=\"images\/" + "word-icon" + ".svg\"><\/img><br><span data-toggle=\"tooltip\" class=\"File-labels\" title=\"" + name + "\"> " + sort + "<\/span><\/div>");
            break;
        case "doc":
            $("#contents").append("<div id=\"row-files\" class=\"icon\" onClick=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" loc=\"" + loc + "\" name=\"" + name + "\" type=\"" + type + "\"><img src=\"images\/" + "word-icon" + ".svg\"><\/img><br><span data-toggle=\"tooltip\" class=\"File-labels\" title=\"" + name + "\"> " + sort + "<\/span><\/div>");
            break;
        case "docx":
            $("#contents").append("<div id=\"row-files\" class=\"icon\" onClick=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" loc=\"" + loc + "\" name=\"" + name + "\" type=\"" + type + "\"><img src=\"images\/" + "word-icon" + ".svg\"><\/img><br><span data-toggle=\"tooltip\" class=\"File-labels\" title=\"" + name + "\"> " + sort + "<\/span><\/div>");
            break;
        case "docs":
            $("#contents").append("<div id=\"row-files\" class=\"icon\" onClick=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" loc=\"" + loc + "\" name=\"" + name + "\" type=\"" + type + "\"><img src=\"images\/" + "word-icon" + ".svg\"><\/img><br><span data-toggle=\"tooltip\" class=\"File-labels\" title=\"" + name + "\"> " + sort + "<\/span><\/div>");
            break;
        case "pages":
            $("#contents").append("<div id=\"row-files\" class=\"icon\" onClick=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" loc=\"" + loc + "\" name=\"" + name + "\" type=\"" + type + "\"><img src=\"images\/" + "word-icon" + ".svg\"><\/img><br><span data-toggle=\"tooltip\" class=\"File-labels\" title=\"" + name + "\"> " + sort + "<\/span><\/div>");
            break;
        case "xls":
            $("#contents").append("<div id=\"row-files\" class=\"icon\" onClick=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" loc=\"" + loc + "\" name=\"" + name + "\" type=\"" + type + "\"><img src=\"images\/" + "excel-icon" + ".svg\"><\/img><br><span data-toggle=\"tooltip\" class=\"File-labels\" title=\"" + name + "\"> " + sort + "<\/span><\/div>");
            break;
        case "numbers":
            $("#contents").append("<div id=\"row-files\" class=\"icon\" onClick=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" loc=\"" + loc + "\" name=\"" + name + "\" type=\"" + type + "\"><img src=\"images\/" + "excel-icon" + ".svg\"><\/img><br><span data-toggle=\"tooltip\" class=\"File-labels\" title=\"" + name + "\"> " + sort + "<\/span><\/div>");
            break;
        case "xlsx":
            $("#contents").append("<div id=\"row-files\" class=\"icon\" onClick=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" loc=\"" + loc + "\" name=\"" + name + "\" type=\"" + type + "\"><img src=\"images\/" + "excel-icon" + ".svg\"><\/img><br><span data-toggle=\"tooltip\" class=\"File-labels\" title=\"" + name + "\"> " + sort + "<\/span><\/div>");
            break;
        case "csv":
            $("#contents").append("<div id=\"row-files\" class=\"icon\" onClick=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" loc=\"" + loc + "\" name=\"" + name + "\" type=\"" + type + "\"><img src=\"images\/" + "excel-icon" + ".svg\"><\/img><br><span data-toggle=\"tooltip\" class=\"File-labels\" title=\"" + name + "\"> " + sort + "<\/span><\/div>");
            break;
        case "ppt":
            $("#contents").append("<div id=\"row-files\" class=\"icon\" onClick=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" loc=\"" + loc + "\" name=\"" + name + "\" type=\"" + type + "\"><img src=\"images\/" + "presentation-icon" + ".svg\"><\/img><br><span data-toggle=\"tooltip\" class=\"File-labels\" title=\"" + name + "\"> " + sort + "<\/span><\/div>");
            break;
        case "keynote":
            $("#contents").append("<div id=\"row-files\" class=\"icon\" onClick=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" loc=\"" + loc + "\" name=\"" + name + "\" type=\"" + type + "\"><img src=\"images\/" + "presentation-icon" + ".svg\"><\/img><br><span data-toggle=\"tooltip\" class=\"File-labels\" title=\"" + name + "\"> " + sort + "<\/span><\/div>");
            break;
        case "pptm":
            $("#contents").append("<div id=\"row-files\" class=\"icon\" onClick=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" loc=\"" + loc + "\" name=\"" + name + "\" type=\"" + type + "\"><img src=\"images\/" + "presentation-icon" + ".svg\"><\/img><br><span data-toggle=\"tooltip\" class=\"File-labels\" title=\"" + name + "\"> " + sort + "<\/span><\/div>");
            break;
        case "pdf":
            $("#contents").append("<div id=\"row-files\" class=\"icon\" onClick=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" loc=\"" + loc + "\" name=\"" + name + "\" type=\"" + type + "\"><img src=\"images\/" + "pdf-icon" + ".svg\"><\/img><br><span data-toggle=\"tooltip\" class=\"File-labels\" title=\"" + name + "\"> " + sort + "<\/span><\/div>");
            break;

        default:
            $("#contents").append("<div id=\"row-files\" class=\"icon\" onClick=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" loc=\"" + loc + "\" name=\"" + name + "\" type=\"" + type + "\"><img src=\"images\/" + "file-icon" + ".svg\"><\/img><br><span data-toggle=\"tooltip\" class=\"File-labels\" title=\"" + name + "\"> " + sort + "<\/span><\/div>");
    }
}
