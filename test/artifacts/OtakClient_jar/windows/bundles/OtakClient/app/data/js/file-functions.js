var filesQueue = 0;

function openPlayerSelect(url) {
    document.getElementById("stream-url").value = url;
    $('#playerSelect').modal('toggle');
}

function startPlayer(type) {
    home.launchPlayer(document.getElementById("stream-url").value, type);
}

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

function addFileProgress(file, status) {
    var filename = file.split(".")[0];
    if (!document.getElementById("tr" + filename)) {
        var span;
        switch (status) {
            case "error":
                span = "<span id='" + "sp" + filename +
                    "' class='label label-danger'>Error</span>";
                break;
            case "upload":
                span = "<span id='" + "sp" + filename +
                    "' class='label label-success'>Uploading</span>";
                break;
            case "download":
                span = "<span id='" + "sp" + filename +
                    "' class='label label-success'>Downloading</span>";
                break;
            case "delete":
                span = "<span id='" + "sp" + filename +
                    "' class='label label-success'>Deleting</span>";
                break;
            case "queue":
                span = "<span id='" + "sp" + filename +
                    "' class='label label-warning'>In queue</span>";
                break;
            case "torrent":
                span = "<span id='" + "sp" + filename +
                    "' class='label label-success'>Torrenting</span>";
                break;
        }
        $("#filesinsync").append("<tr id='tr" + filename + "'> \
		  <td>" +
            file + "</td> \
		  <td class='pbar'>" + span +
            "</td> \
		  </tr>");
        filesQueue++;
        updateSync();
    } else {
        changeStatus(file, status);
    }
}

function changeStatus(file, status) {
    var filename = file.split(".")[0];
    var span = document.getElementById("sp" + filename);
    switch (status) {
        case "error":
            span.className = "label label-danger";
            span.textContent = "Error"
            break;
        case "upload":
            span.className = "label label-success";
            span.textContent = "Uploading"
            break;
        case "download":
            span.className = "label label-success";
            span.textContent = "Downloading"
            break;
        case "queue":
            span.className = "label label-warning";
            span.textContent = "In queue"
            break;
        case "torrent":
            span.className = "label label-success";
            span.textContent = "Torrenting"
            break;
    }
}

function removeFileProgress(file) {
    var filename = file.split(".")[0];
    if (document.getElementById("tr" + filename)) {
        document.getElementById("tr" + filename).remove();
        filesQueue--;
        updateSync();
    }
}

function updateSync() {
    var syncinfo = $("#syncinfo");
    if (filesQueue == 0) {
        syncinfo.text("No downloads");
    } else {
        syncinfo.text("Downloading: " + filesQueue + " files");
    }
}

function removeItem(loc) {
    var item = $("div[loc=\"" + loc + "\"]");
    item.remove();
}

function clearItems() {
    var myNode = document.getElementById("contents");
    while (myNode.firstChild) {
        myNode.removeChild(myNode.firstChild);
    }
}
String.prototype.truncate = function(n) {
    return this.substr(0, n - 1) + (this.length > n ? '&hellip;' : '');
};

function onRowClick(event) {
    var source = event.target.parentElement;
    if (event.which == 1) {
        home.onClick('left', source.getAttribute("loc"), source.getAttribute(
            "name"), source.getAttribute("type"));
    }
    if (event.which == 3) {
        home.onClick('right', source.getAttribute("loc"), source.getAttribute(
            "name"), source.getAttribute("type"));
    }
}

function addItem(loc, name, type) {
    sort = name.truncate(16);
    switch (type) {
        case "folder":
            $("#contents").append(
                "<div id=\"row-files\" class=\"icon\" onClick=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" loc=\"" +
                loc + "\" name=\"" + name + "\" type=\"" + type +
                "\"><img src=\"images\/" + "folder-icon" +
                ".svg\"><\/img><br><span data-toggle=\"tooltip\" class=\"File-labels\" title=\"" +
                name + "\"> " + sort + "<\/span><\/div>");
            break;
        case "png":
            $("#contents").append(
                "<div id=\"row-files\" class=\"icon\" onClick=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" loc=\"" +
                loc + "\" name=\"" + name + "\" type=\"" + type +
                "\"><img src=\"images\/" + "photo-icon" +
                ".svg\"><\/img><br><span data-toggle=\"tooltip\" class=\"File-labels\" title=\"" +
                name + "\"> " + sort + "<\/span><\/div>");
            break;
        case "gif":
            $("#contents").append(
                "<div id=\"row-files\" class=\"icon\" onClick=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" loc=\"" +
                loc + "\" name=\"" + name + "\" type=\"" + type +
                "\"><img src=\"images\/" + "gif-icon" +
                ".svg\"><\/img><br><span data-toggle=\"tooltip\" class=\"File-labels\" title=\"" +
                name + "\"> " + sort + "<\/span><\/div>");
            break;
        case "jpeg":
            $("#contents").append(
                "<div id=\"row-files\" class=\"icon\" onClick=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" loc=\"" +
                loc + "\" name=\"" + name + "\" type=\"" + type +
                "\"><img src=\"images\/" + "photo-icon" +
                ".svg\"><\/img><br><span data-toggle=\"tooltip\" class=\"File-labels\" title=\"" +
                name + "\"> " + sort + "<\/span><\/div>");
            break;
        case "jpg":
            $("#contents").append(
                "<div id=\"row-files\" class=\"icon\" onClick=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" loc=\"" +
                loc + "\" name=\"" + name + "\" type=\"" + type +
                "\"><img src=\"images\/" + "photo-icon" +
                ".svg\"><\/img><br><span data-toggle=\"tooltip\" class=\"File-labels\" title=\"" +
                name + "\"> " + sort + "<\/span><\/div>");
            break;
        case "tiff":
            $("#contents").append(
                "<div id=\"row-files\" class=\"icon\" onClick=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" loc=\"" +
                loc + "\" name=\"" + name + "\" type=\"" + type +
                "\"><img src=\"images\/" + "photo-icon" +
                ".svg\"><\/img><br><span data-toggle=\"tooltip\" class=\"File-labels\" title=\"" +
                name + "\"> " + sort + "<\/span><\/div>");
            break;
        case "zip":
            $("#contents").append(
                "<div id=\"row-files\" class=\"icon\" onClick=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" loc=\"" +
                loc + "\" name=\"" + name + "\" type=\"" + type +
                "\"><img src=\"images\/" + "zip-icon" +
                ".svg\"><\/img><br><span data-toggle=\"tooltip\" class=\"File-labels\" title=\"" +
                name + "\"> " + sort + "<\/span><\/div>");
            break;
        case "tar":
            $("#contents").append(
                "<div id=\"row-files\" class=\"icon\" onClick=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" loc=\"" +
                loc + "\" name=\"" + name + "\" type=\"" + type +
                "\"><img src=\"images\/" + "zip-icon" +
                ".svg\"><\/img><br><span data-toggle=\"tooltip\" class=\"File-labels\" title=\"" +
                name + "\"> " + sort + "<\/span><\/div>");
            break;
        case "mp4":
            $("#contents").append(
                "<div id=\"row-files\" class=\"icon\" onClick=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" loc=\"" +
                loc + "\" name=\"" + name + "\" type=\"" + type +
                "\"><img src=\"images\/" + "video-icon" +
                ".svg\"><\/img><br><span data-toggle=\"tooltip\" class=\"File-labels\" title=\"" +
                name + "\"> " + sort + "<\/span><\/div>");
            break;
        case "avi":
            $("#contents").append(
                "<div id=\"row-files\" class=\"icon\" onClick=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" loc=\"" +
                loc + "\" name=\"" + name + "\" type=\"" + type +
                "\"><img src=\"images\/" + "video-icon" +
                ".svg\"><\/img><br><span data-toggle=\"tooltip\" class=\"File-labels\" title=\"" +
                name + "\"> " + sort + "<\/span><\/div>");
            break;
        case "mpg":
            $("#contents").append(
                "<div id=\"row-files\" class=\"icon\" onClick=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" loc=\"" +
                loc + "\" name=\"" + name + "\" type=\"" + type +
                "\"><img src=\"images\/" + "video-icon" +
                ".svg\"><\/img><br><span data-toggle=\"tooltip\" class=\"File-labels\" title=\"" +
                name + "\"> " + sort + "<\/span><\/div>");
            break;
        case "mov":
            $("#contents").append(
                "<div id=\"row-files\" class=\"icon\" onClick=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" loc=\"" +
                loc + "\" name=\"" + name + "\" type=\"" + type +
                "\"><img src=\"images\/" + "video-icon" +
                ".svg\"><\/img><br><span data-toggle=\"tooltip\" class=\"File-labels\" title=\"" +
                name + "\"> " + sort + "<\/span><\/div>");
            break;
        case "mkv":
            $("#contents").append(
                "<div id=\"row-files\" class=\"icon\" onClick=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" loc=\"" +
                loc + "\" name=\"" + name + "\" type=\"" + type +
                "\"><img src=\"images\/" + "video-icon" +
                ".svg\"><\/img><br><span data-toggle=\"tooltip\" class=\"File-labels\" title=\"" +
                name + "\"> " + sort + "<\/span><\/div>");
            break;
        case "mp3":
            $("#contents").append(
                "<div id=\"row-files\" class=\"icon\" onClick=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" loc=\"" +
                loc + "\" name=\"" + name + "\" type=\"" + type +
                "\"><img src=\"images\/" + "music-icon" +
                ".svg\"><\/img><br><span data-toggle=\"tooltip\" class=\"File-labels\" title=\"" +
                name + "\"> " + sort + "<\/span><\/div>");
            break;
        case "jar":
            $("#contents").append(
                "<div id=\"row-files\" class=\"icon\" onClick=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" loc=\"" +
                loc + "\" name=\"" + name + "\" type=\"" + type +
                "\"><img src=\"images\/" + "java-icon" +
                ".svg\"><\/img><br><span data-toggle=\"tooltip\" class=\"File-labels\" title=\"" +
                name + "\"> " + sort + "<\/span><\/div>");
            break;
        case "ai":
            $("#contents").append(
                "<div id=\"row-files\" class=\"icon\" onClick=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" loc=\"" +
                loc + "\" name=\"" + name + "\" type=\"" + type +
                "\"><img src=\"images\/" + "illustrator-icon" +
                ".svg\"><\/img><br><span data-toggle=\"tooltip\" class=\"File-labels\" title=\"" +
                name + "\"> " + sort + "<\/span><\/div>");
            break;
        case "psd":
            $("#contents").append(
                "<div id=\"row-files\" class=\"icon\" onClick=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" loc=\"" +
                loc + "\" name=\"" + name + "\" type=\"" + type +
                "\"><img src=\"images\/" + "photoshop-icon" +
                ".svg\"><\/img><br><span data-toggle=\"tooltip\" class=\"File-labels\" title=\"" +
                name + "\"> " + sort + "<\/span><\/div>");
            break;
        case "txt":
            $("#contents").append(
                "<div id=\"row-files\" class=\"icon\" onClick=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" loc=\"" +
                loc + "\" name=\"" + name + "\" type=\"" + type +
                "\"><img src=\"images\/" + "word-icon" +
                ".svg\"><\/img><br><span data-toggle=\"tooltip\" class=\"File-labels\" title=\"" +
                name + "\"> " + sort + "<\/span><\/div>");
            break;
        case "doc":
            $("#contents").append(
                "<div id=\"row-files\" class=\"icon\" onClick=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" loc=\"" +
                loc + "\" name=\"" + name + "\" type=\"" + type +
                "\"><img src=\"images\/" + "word-icon" +
                ".svg\"><\/img><br><span data-toggle=\"tooltip\" class=\"File-labels\" title=\"" +
                name + "\"> " + sort + "<\/span><\/div>");
            break;
        case "docx":
            $("#contents").append(
                "<div id=\"row-files\" class=\"icon\" onClick=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" loc=\"" +
                loc + "\" name=\"" + name + "\" type=\"" + type +
                "\"><img src=\"images\/" + "word-icon" +
                ".svg\"><\/img><br><span data-toggle=\"tooltip\" class=\"File-labels\" title=\"" +
                name + "\"> " + sort + "<\/span><\/div>");
            break;
        case "docs":
            $("#contents").append(
                "<div id=\"row-files\" class=\"icon\" onClick=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" loc=\"" +
                loc + "\" name=\"" + name + "\" type=\"" + type +
                "\"><img src=\"images\/" + "word-icon" +
                ".svg\"><\/img><br><span data-toggle=\"tooltip\" class=\"File-labels\" title=\"" +
                name + "\"> " + sort + "<\/span><\/div>");
            break;
        case "pages":
            $("#contents").append(
                "<div id=\"row-files\" class=\"icon\" onClick=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" loc=\"" +
                loc + "\" name=\"" + name + "\" type=\"" + type +
                "\"><img src=\"images\/" + "word-icon" +
                ".svg\"><\/img><br><span data-toggle=\"tooltip\" class=\"File-labels\" title=\"" +
                name + "\"> " + sort + "<\/span><\/div>");
            break;
        case "xls":
            $("#contents").append(
                "<div id=\"row-files\" class=\"icon\" onClick=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" loc=\"" +
                loc + "\" name=\"" + name + "\" type=\"" + type +
                "\"><img src=\"images\/" + "excel-icon" +
                ".svg\"><\/img><br><span data-toggle=\"tooltip\" class=\"File-labels\" title=\"" +
                name + "\"> " + sort + "<\/span><\/div>");
            break;
        case "numbers":
            $("#contents").append(
                "<div id=\"row-files\" class=\"icon\" onClick=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" loc=\"" +
                loc + "\" name=\"" + name + "\" type=\"" + type +
                "\"><img src=\"images\/" + "excel-icon" +
                ".svg\"><\/img><br><span data-toggle=\"tooltip\" class=\"File-labels\" title=\"" +
                name + "\"> " + sort + "<\/span><\/div>");
            break;
        case "xlsx":
            $("#contents").append(
                "<div id=\"row-files\" class=\"icon\" onClick=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" loc=\"" +
                loc + "\" name=\"" + name + "\" type=\"" + type +
                "\"><img src=\"images\/" + "excel-icon" +
                ".svg\"><\/img><br><span data-toggle=\"tooltip\" class=\"File-labels\" title=\"" +
                name + "\"> " + sort + "<\/span><\/div>");
            break;
        case "csv":
            $("#contents").append(
                "<div id=\"row-files\" class=\"icon\" onClick=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" loc=\"" +
                loc + "\" name=\"" + name + "\" type=\"" + type +
                "\"><img src=\"images\/" + "excel-icon" +
                ".svg\"><\/img><br><span data-toggle=\"tooltip\" class=\"File-labels\" title=\"" +
                name + "\"> " + sort + "<\/span><\/div>");
            break;
        case "ppt":
            $("#contents").append(
                "<div id=\"row-files\" class=\"icon\" onClick=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" loc=\"" +
                loc + "\" name=\"" + name + "\" type=\"" + type +
                "\"><img src=\"images\/" + "presentation-icon" +
                ".svg\"><\/img><br><span data-toggle=\"tooltip\" class=\"File-labels\" title=\"" +
                name + "\"> " + sort + "<\/span><\/div>");
            break;
        case "keynote":
            $("#contents").append(
                "<div id=\"row-files\" class=\"icon\" onClick=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" loc=\"" +
                loc + "\" name=\"" + name + "\" type=\"" + type +
                "\"><img src=\"images\/" + "presentation-icon" +
                ".svg\"><\/img><br><span data-toggle=\"tooltip\" class=\"File-labels\" title=\"" +
                name + "\"> " + sort + "<\/span><\/div>");
            break;
        case "pptm":
            $("#contents").append(
                "<div id=\"row-files\" class=\"icon\" onClick=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" loc=\"" +
                loc + "\" name=\"" + name + "\" type=\"" + type +
                "\"><img src=\"images\/" + "presentation-icon" +
                ".svg\"><\/img><br><span data-toggle=\"tooltip\" class=\"File-labels\" title=\"" +
                name + "\"> " + sort + "<\/span><\/div>");
            break;
        case "pdf":
            $("#contents").append(
                "<div id=\"row-files\" class=\"icon\" onClick=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" loc=\"" +
                loc + "\" name=\"" + name + "\" type=\"" + type +
                "\"><img src=\"images\/" + "pdf-icon" +
                ".svg\"><\/img><br><span data-toggle=\"tooltip\" class=\"File-labels\" title=\"" +
                name + "\"> " + sort + "<\/span><\/div>");
            break;
        case "css":
            $("#contents").append(
                "<div id=\"row-files\" class=\"icon\" onClick=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" loc=\"" +
                loc + "\" name=\"" + name + "\" type=\"" + type +
                "\"><img src=\"images\/" + "code-icon" +
                ".svg\"><\/img><br><span data-toggle=\"tooltip\" class=\"File-labels\" title=\"" +
                name + "\"> " + sort + "<\/span><\/div>");
            break;
        case "html":
            $("#contents").append(
                "<div id=\"row-files\" class=\"icon\" onClick=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" loc=\"" +
                loc + "\" name=\"" + name + "\" type=\"" + type +
                "\"><img src=\"images\/" + "code-icon" +
                ".svg\"><\/img><br><span data-toggle=\"tooltip\" class=\"File-labels\" title=\"" +
                name + "\"> " + sort + "<\/span><\/div>");
            break;
        case "js":
            $("#contents").append(
                "<div id=\"row-files\" class=\"icon\" onClick=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" loc=\"" +
                loc + "\" name=\"" + name + "\" type=\"" + type +
                "\"><img src=\"images\/" + "code-icon" +
                ".svg\"><\/img><br><span data-toggle=\"tooltip\" class=\"File-labels\" title=\"" +
                name + "\"> " + sort + "<\/span><\/div>");
            break;
        case "py":
            $("#contents").append(
                "<div id=\"row-files\" class=\"icon\" onClick=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" loc=\"" +
                loc + "\" name=\"" + name + "\" type=\"" + type +
                "\"><img src=\"images\/" + "code-icon" +
                ".svg\"><\/img><br><span data-toggle=\"tooltip\" class=\"File-labels\" title=\"" +
                name + "\"> " + sort + "<\/span><\/div>");
            break;
        default:
            $("#contents").append(
                "<div id=\"row-files\" class=\"icon\" onClick=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" oncontextmenu=\"onRowClick(event);\" loc=\"" +
                loc + "\" name=\"" + name + "\" type=\"" + type +
                "\"><img src=\"images\/" + "file-icon" +
                ".svg\"><\/img><br><span data-toggle=\"tooltip\" class=\"File-labels\" title=\"" +
                name + "\"> " + sort + "<\/span><\/div>");
    }
}

function setServerName(name) {
    document.getElementById("servername").textContent = name;
}