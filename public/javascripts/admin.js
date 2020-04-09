/* global variables */
var detailedData = {};
var currentMenu = null;

/* show busy searching */
function showBusySearching(cleanup) {
    if (cleanup) {
        document.getElementById("data").innerHTML = "";
    }
    document.getElementById("searching-busy").style.visibility = "visible";
    document.getElementById("searching-successful").style.visibility = "hidden";
}

/* dismiss busy searching */
function dismissBusySearching() {
    document.getElementById("searching-busy").style.visibility = "hidden";
    document.getElementById("searching-successful").style.visibility = "visible";
}

/* show input warning message */
function showInputWarningMessage(message) {
    document.getElementById("user-warning").innerHTML = '<i class="exclamation triangle icon"></i> ' + message;
    document.getElementById("user-warning").style.display = "";
    document.getElementById("board-warning").innerHTML = '<i class="exclamation triangle icon"></i> ' + message;
    document.getElementById("board-warning").style.display = "";
    document.getElementById("post-warning").innerHTML = '<i class="exclamation triangle icon"></i> ' + message;
    document.getElementById("post-warning").style.display = "";
    document.getElementById("comment-warning").innerHTML = '<i class="exclamation triangle icon"></i> ' + message;
    document.getElementById("comment-warning").style.display = "";
}

/* dismiss input warning message */
function dismissInputWarningMessage() {
    document.getElementById("user-warning").innerHTML = '<i class="exclamation triangle icon"></i> ';
    document.getElementById("user-warning").style.display = "none";
    document.getElementById("board-warning").innerHTML = '<i class="exclamation triangle icon"></i> ';
    document.getElementById("board-warning").style.display = "none";
    document.getElementById("post-warning").innerHTML = '<i class="exclamation triangle icon"></i> ';
    document.getElementById("post-warning").style.display = "none";
    document.getElementById("comment-warning").innerHTML = '<i class="exclamation triangle icon"></i> ';
    document.getElementById("comment-warning").style.display = "none";
}

/* view users */
function viewUsers(search) {
    connectAndUpdate("/api/v1/admin/users", "POST", {"search": search}, rsp => {
        var keyIcon = ' <img src="/icons/key.svg" style="filter:invert(100%)"/> ';
        var div = document.getElementById("data");
        detailedData = {};
        rsp.data.forEach(e => {
            var row = div.insertRow(div.rows.length);
            row.insertCell(0).innerHTML = e.id;
            row.insertCell(1).innerHTML = e.name + keyIcon + e.account + "/" + e.password;
            row.innerHTML += '<td class="selectable"><a onclick="popUserInfo(' + e.id  + ')"><i class="edit outline icon"></i></a></td>';
            detailedData[e.id.toString()] = e;
        });
        dismissBusySearching();
        $("#searching-successful").progress({autoSuccess: false, value: rsp.data.length, total: rsp.total, text: {percent: "showing {value} of {total}"}});
    });
}

/* view boards */
function viewBoards(search) {
    connectAndUpdate("/api/v1/admin/boards", "POST", {"search": search}, rsp => {
        var div = document.getElementById("data");
        detailedData = {};
        rsp.data.forEach(e => {
            var row = div.insertRow(div.rows.length);
            row.insertCell(0).innerHTML = e.id;
            row.insertCell(1).innerHTML = e.name;
            row.innerHTML += '<td class="selectable"><a onclick="popBoardInfo(' + e.id  + ')"><i class="edit outline icon"></i></a></td>';
            detailedData[e.id.toString()] = e;
        });
        dismissBusySearching();
        $("#searching-successful").progress({autoSuccess: false, value: rsp.data.length, total: rsp.total, text: {percent: "showing {value} of {total}"}});
    });
}

/* view posts */
function viewPosts(search) {
    connectAndUpdate("/api/v1/admin/posts", "POST", {"search": search}, rsp =>  {
        var calendarIcon = ' <img src="/icons/calendar.svg" style="filter:invert(100%)"/> ';
        var div = document.getElementById("data");
        detailedData = {};
        rsp.data.forEach(e => {
            var row = div.insertRow(div.rows.length);
            row.insertCell(0).innerHTML = e.id;
            row.insertCell(1).innerHTML = e.title + " (" + e.content.substring(0, 5) + "...)" + calendarIcon + e.ts_create;
            row.innerHTML += '<td class="selectable"><a onclick="popPostInfo(' + e.id  + ')"><i class="edit outline icon"></i></a></td>';
            detailedData[e.id.toString()] = e;
        });
        dismissBusySearching();
        $("#searching-successful").progress({autoSuccess: false, value: rsp.data.length, total: rsp.total, text: {percent: "showing {value} of {total}"}});
    });
}

/* view comments */
function viewComments(search) {
    connectAndUpdate("/api/v1/admin/comments", "POST", {"search": search}, rsp =>  {
        var calendarIcon = ' <img src="/icons/calendar.svg" style="filter:invert(100%)"/> ';
        var div = document.getElementById("data");
        detailedData = {};
        rsp.data.forEach(e => {
            var row = div.insertRow(div.rows.length);
            row.insertCell(0).innerHTML = e.id;
            row.insertCell(1).innerHTML = e.content.substring(0, 15) + "..." + calendarIcon + e.ts_create;
            row.innerHTML += '<td class="selectable"><a onclick="popCommentInfo(' + e.id  + ')"><i class="edit outline icon"></i></a></td>';
            detailedData[e.id.toString()] = e;
        });
        dismissBusySearching();
        $("#searching-successful").progress({autoSuccess: false, value: rsp.data.length, total: rsp.total, text: {percent: "showing {value} of {total}"}});
    });
}

/* pop user detailed info */
function popUserInfo(id) {
    dismissInputWarningMessage();
    if (id < 0) {
        truncateEveryInputFieldInDiv("user-data");
        document.getElementById("user-delete").style.display = "none";
    }
    else {
        var user = detailedData[id.toString()];
        document.getElementById("user-id").value = user.id;
        document.getElementById("user-account").value = user.account;
        document.getElementById("user-password").value = user.password;
        document.getElementById("user-name").value = user.name;
        document.getElementById("user-type").value = user.type;
        document.getElementById("user-location").value = user.location;
        document.getElementById("user-motto").value = user.motto;
        document.getElementById("user-url_avatar").value = user.url_avatar;
        document.getElementById("user-intro").value = user.intro;
        document.getElementById("user-delete").style.display = "";
    }
    document.getElementById("user-update").setAttribute("onclick", "createOrUpdateUser(" + id + ")");
    document.getElementById("user-delete").setAttribute("onclick", "deleteUserRecursively(" + id + ")");
    $("#user-data").modal({onApprove: function() {return false;}}).modal("show");
}

/* pop board detailed info */
function popBoardInfo(id) {
    dismissInputWarningMessage();
    if (id < 0) {
        truncateEveryInputFieldInDiv("board-data");
        document.getElementById("board-delete").style.display = "none";
    }
    else {
        var board = detailedData[id.toString()];
        document.getElementById("board-id").value = board.id;
        document.getElementById("board-name").value = board.name;
        document.getElementById("board-delete").style.display = "";
    }
    document.getElementById("board-update").setAttribute("onclick", "createOrUpdateBoard(" + id + ")");
    document.getElementById("board-delete").setAttribute("onclick", "deleteBoardRecursively(" + id + ")");
    $("#board-data").modal({onApprove: function() {return false;}}).modal("show");
}

/* pop post detailed info */
function popPostInfo(id) {
    dismissInputWarningMessage();
    if (id < 0) {
        truncateEveryInputFieldInDiv("post-data");
        document.getElementById("post-delete").style.display = "none";
    }
    else {
        var post = detailedData[id.toString()];
        document.getElementById("post-id").value = post.id;
        document.getElementById("post-id_user").value = post.id_user;
        document.getElementById("post-id_board").value = post.id_board;
        document.getElementById("post-title").value = post.title;
        document.getElementById("post-url_avatar").value = post.url_avatar;
        document.getElementById("post-ts_create").value = post.ts_create;
        document.getElementById("post-content").value = post.content;
        document.getElementById("post-delete").style.display = "";
    }
    document.getElementById("post-update").setAttribute("onclick", "createOrUpdatePost(" + id + ")");
    document.getElementById("post-delete").setAttribute("onclick", "deletePostRecursively(" + id + ")");
    $("#post-data").modal({onApprove: function() {return false;}}).modal("show");
}

/* pop comment detailed info */
function popCommentInfo(id) {
    dismissInputWarningMessage();
    if (id < 0) {
        truncateEveryInputFieldInDiv("comment-data");
        document.getElementById("comment-delete").style.display = "none";
    }
    else {
        var comment = detailedData[id.toString()];
        document.getElementById("comment-id").value = comment.id;
        document.getElementById("comment-id_user").value = comment.id_user;
        document.getElementById("comment-id_post").value = comment.id_post;
        document.getElementById("comment-ts_create").value = comment.ts_create;
        document.getElementById("comment-content").value = comment.content;
        document.getElementById("comment-delete").style.display = "";
    }
    document.getElementById("comment-update").setAttribute("onclick", "createOrUpdateComment(" + id + ")");
    document.getElementById("comment-delete").setAttribute("onclick", "deleteCommentRecursively(" + id + ")");
    $("#comment-data").modal({onApprove: function() {return false;}}).modal("show");
}

/* create or update user */
function createOrUpdateUser(id) {
    if (isAnyInputFieldEmptyInDiv("user-data")) {
        showInputWarningMessage("Please setup all the information except for the auto-generated ID. Empty value is not allowed.");
        return;
    }
    var newInfo = {
        "id": id,
        "account": document.getElementById("user-account").value.trim(),
        "password": document.getElementById("user-password").value.trim(),
        "name": document.getElementById("user-name").value.trim(),
        "type": document.getElementById("user-type").value.trim(),
        "location": document.getElementById("user-location").value.trim(),
        "motto": document.getElementById("user-motto").value.trim(),
        "url_avatar": document.getElementById("user-url_avatar").value.trim(),
        "intro": document.getElementById("user-intro").value.trim()
    };
    showBusySearching(true);
    connectAndUpdate("/api/v1/admin/user/update", "POST", newInfo, rsp => {
        if (!document.getElementById("keyword").value.trim()) {
            document.getElementById("keyword").value = rsp.id;
        }
        currentMenu.click();
    });
    $("#user-data").modal("hide");
}

/* create or update board */
function createOrUpdateBoard(id) {
    if (isAnyInputFieldEmptyInDiv("board-data")) {
        showInputWarningMessage("Please setup all the information except for the auto-generated ID. Empty value is not allowed.");
        return;
    }
    var newInfo = {
        "id": id,
        "name": document.getElementById("board-name").value.trim()
    };
    showBusySearching(true);
    connectAndUpdate("/api/v1/admin/board/update", "POST", newInfo, rsp => {
        if (!document.getElementById("keyword").value.trim()) {
            document.getElementById("keyword").value = rsp.id;
        }
        currentMenu.click();
    });
    $("#board-data").modal("hide");
}

/* create or update post */
function createOrUpdatePost(id) {
    if (isAnyInputFieldEmptyInDiv("post-data")) {
        showInputWarningMessage("Please setup all the required information except for the auto-generated ID. Empty value is not allowed.");
        return;
    }
    if (!new RegExp(/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}$/).test(document.getElementById("post-ts_create").value.trim())) {
        showInputWarningMessage("Time format should be YYYY-MM-DD hh:mm (e.g., 1999-01-31 23:59). Please check the input again.");
        return;
    }
    var newInfo = {
        "id": id,
        "id_user": document.getElementById("post-id_user").value,
        "id_board": document.getElementById("post-id_board").value,
        "title": document.getElementById("post-title").value.trim(),
        "content": document.getElementById("post-content").value.trim(),
        "url_avatar": document.getElementById("post-url_avatar").value.trim(),
        "ts_create": document.getElementById("post-ts_create").value.trim()
    };
    showBusySearching(true);
    connectAndUpdate("/api/v1/admin/post/update", "POST", newInfo, rsp => {
        if (!document.getElementById("keyword").value.trim()) {
            document.getElementById("keyword").value = rsp.id;
        }
        currentMenu.click();
    });
    $("#post-data").modal("hide");
}

/* create or update comment */
function createOrUpdateComment(id) {
    if (isAnyInputFieldEmptyInDiv("comment-data")) {
        showInputWarningMessage("Please setup all the required information except for the auto-generated ID. Empty value is not allowed.");
        return;
    }
    if (!new RegExp(/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}$/).test(document.getElementById("comment-ts_create").value.trim())) {
        showInputWarningMessage("Time format should be YYYY-MM-DD hh:mm (e.g., 1999-01-31 23:59). Please check the input again.");
        return;
    }
    var newInfo = {
        "id": id,
        "id_user": document.getElementById("comment-id_user").value,
        "id_post": document.getElementById("comment-id_post").value,
        "content": document.getElementById("comment-content").value.trim(),
        "ts_create": document.getElementById("comment-ts_create").value.trim()
    };
    showBusySearching(true);
    connectAndUpdate("/api/v1/admin/comment/update", "POST", newInfo, rsp => {
        if (!document.getElementById("keyword").value.trim()) {
            document.getElementById("keyword").value = rsp.id;
        }
        currentMenu.click();
    });
    $("#comment-data").modal("hide");
}

/* delete user recursively */
function deleteUserRecursively(id) {
    showBusySearching(true);
    connectAndUpdate("/api/v1/admin/user/delete", "POST", {"id": id}, rsp => {
        currentMenu.click();
    });
    $("#user-data").modal("hide");
}

/* delete board recursively */
function deleteBoardRecursively(id) {
    showBusySearching(true);
    connectAndUpdate("/api/v1/admin/board/delete", "POST", {"id": id}, rsp => {
        currentMenu.click();
    });
    $("#board-data").modal("hide");
}

/* delete post recursively */
function deletePostRecursively(id) {
    showBusySearching(true);
    connectAndUpdate("/api/v1/admin/post/delete", "POST", {"id": id}, rsp => {
        currentMenu.click();
    });
    $("#post-data").modal("hide");
}

/* delete comment recursively */
function deleteCommentRecursively(id) {
    showBusySearching(true);
    connectAndUpdate("/api/v1/admin/comment/delete", "POST", {"id": id}, rsp => {
        currentMenu.click();
    });
    $("#comment-data").modal("hide");
}

/* dump users to csv */
function dumpUsers() {
    showBusySearching(false);
    connectAndUpdate("/api/v1/admin/users/dump", "POST", {}, rsp => {
        triggerDownload("users.csv", rsp.url);
        dismissBusySearching();
    });
}

/* dump boards to csv */
function dumpBoards() {
    showBusySearching(false);
    connectAndUpdate("/api/v1/admin/boards/dump", "POST", {}, rsp => {
        triggerDownload("boards.csv", rsp.url);
        dismissBusySearching();
    });
}

/* dump posts to csv */
function dumpPosts() {
    showBusySearching(false);
    connectAndUpdate("/api/v1/admin/posts/dump", "POST", {}, rsp => {
        triggerDownload("posts.csv", rsp.url);
        dismissBusySearching();
    });
}

/* dump comments to csv */
function dumpComments() {
    showBusySearching(false);
    connectAndUpdate("/api/v1/admin/comments/dump", "POST", {}, rsp => {
        triggerDownload("comments.csv", rsp.url);
        dismissBusySearching();
    });
}

/* document ready */
document.addEventListener("DOMContentLoaded", function(){
    /* onclick listeners */
    document.getElementById("users").addEventListener("click", function() {
        showBusySearching(true);
        currentMenu.className = "item";
        currentMenu = this;
        currentMenu.className = "active item";
        document.getElementById("export").setAttribute("onclick", "dumpUsers()");
        document.getElementById("new").setAttribute("onclick", "popUserInfo(-1)");
        setTimeout(function(){ viewUsers(document.getElementById("keyword").value.trim()); }, 1000);
    });
    document.getElementById("boards").addEventListener("click", function() {
        showBusySearching(true);
        currentMenu.className = "item";
        currentMenu = this;
        currentMenu.className = "active item";
        document.getElementById("export").setAttribute("onclick", "dumpBoards()");
        document.getElementById("new").setAttribute("onclick", "popBoardInfo(-1)");
        setTimeout(function(){ viewBoards(document.getElementById("keyword").value.trim()); }, 1000);
    });
    document.getElementById("posts").addEventListener("click", function() {
        showBusySearching(true);
        currentMenu.className = "item";
        currentMenu = this;
        currentMenu.className = "active item";
        document.getElementById("export").setAttribute("onclick", "dumpPosts()");
        document.getElementById("new").setAttribute("onclick", "popPostInfo(-1)");
        setTimeout(function(){ viewPosts(document.getElementById("keyword").value.trim()); }, 1000);
    });
    document.getElementById("comments").addEventListener("click", function() {
        showBusySearching(true);
        currentMenu.className = "item";
        currentMenu = this;
        currentMenu.className = "active item";
        document.getElementById("export").setAttribute("onclick", "dumpComments()");
        document.getElementById("new").setAttribute("onclick", "popCommentInfo(-1)");
        setTimeout(function(){ viewComments(document.getElementById("keyword").value.trim()); }, 1000);
    });
    document.getElementById("search").addEventListener("click", function() {
        currentMenu.click();
    });
    /* default */
    currentMenu = document.getElementById("users");
    currentMenu.click();
});
