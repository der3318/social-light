/* global variables */
var detailedData = {};

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
        document.getElementById("searching-busy").style.visibility = "hidden";
        document.getElementById("searching-successful").style.visibility = "visible";
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
        document.getElementById("searching-busy").style.visibility = "hidden";
        document.getElementById("searching-successful").style.visibility = "visible";
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
        document.getElementById("searching-busy").style.visibility = "hidden";
        document.getElementById("searching-successful").style.visibility = "visible";
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
        document.getElementById("searching-busy").style.visibility = "hidden";
        document.getElementById("searching-successful").style.visibility = "visible";
        $("#searching-successful").progress({autoSuccess: false, value: rsp.data.length, total: rsp.total, text: {percent: "showing {value} of {total}"}});
    });
}

/* pop user detailed info */
function popUserInfo(id) {
    if (id < 0) {
        truncateEveryInputFieldInDiv("user-data");
        document.getElementById("user-id").value = "AUTO-GENERATED";
        document.getElementById("user-delete").style.display = "none";
    }
    else {
        let user = detailedData[id.toString()];
        document.getElementById("user-id").value = user.id;
        document.getElementById("user-account").value = user.account;
        document.getElementById("user-password").value = user.password;
        document.getElementById("user-name").value = user.name;
        document.getElementById("user-type").value = user.type;
        document.getElementById("user-location").value = user.location;
        document.getElementById("user-motto").value = user.motto;
        document.getElementById("user-url_avatar").value = user.url_avatar;
        document.getElementById("user-intro").innerHTML = user.intro;
        document.getElementById("user-delete").style.display = "";
    }
    $("#user-data").modal("show");
}

/* pop board detailed info */
function popBoardInfo(id) {
    if (id < 0) {
        truncateEveryInputFieldInDiv("board-data");
        document.getElementById("board-id").value = "AUTO-GENERATED";
        document.getElementById("board-delete").style.display = "none";
    }
    else {
        let board = detailedData[id.toString()];
        document.getElementById("board-id").value = board.id;
        document.getElementById("board-name").value = board.name;
        document.getElementById("board-delete").style.display = "";
    }
    $("#board-data").modal("show");
}

/* pop post detailed info */
function popPostInfo(id) {
    if (id < 0) {
        truncateEveryInputFieldInDiv("post-data");
        document.getElementById("post-id").value = "AUTO-GENERATED";
        document.getElementById("post-delete").style.display = "none";
    }
    else {
        let post = detailedData[id.toString()];
        document.getElementById("post-id").value = post.id;
        document.getElementById("post-id_user").value = post.id_user;
        document.getElementById("post-id_board").value = post.id_board;
        document.getElementById("post-title").value = post.title;
        document.getElementById("post-url_avatar").value = post.url_avatar;
        document.getElementById("post-ts_create").value = post.ts_create;
        document.getElementById("post-content").innerHTML = post.content;
        document.getElementById("post-delete").style.display = "";
    }
    $("#post-data").modal("show");
}

/* pop comment detailed info */
function popCommentInfo(id) {
    if (id < 0) {
        truncateEveryInputFieldInDiv("comment-data");
        document.getElementById("comment-id").value = "AUTO-GENERATED";
        document.getElementById("comment-delete").style.display = "none";
    }
    else {
        let comment = detailedData[id.toString()];
        document.getElementById("comment-id").value = comment.id;
        document.getElementById("comment-id_user").value = comment.id_user;
        document.getElementById("comment-id_post").value = comment.id_post;
        document.getElementById("comment-ts_create").value = comment.ts_create;
        document.getElementById("comment-content").innerHTML = comment.content;
        document.getElementById("comment-delete").style.display = "";
    }
    $("#comment-data").modal("show");
}

/* document ready */
document.addEventListener("DOMContentLoaded", function(){
    /* links */
    var hu = document.getElementById("users"), hb = document.getElementById("boards"), hp = document.getElementById("posts"), hc = document.getElementById("comments");
    var current = hu;
    /* onclick listeners */
    hu.addEventListener("click", function() {
        document.getElementById("data").innerHTML = "";
        document.getElementById("searching-busy").style.visibility = "visible";
        document.getElementById("searching-successful").style.visibility = "hidden";
        current.className = "item";
        current = hu;
        current.className = "active item";
        document.getElementById("new").setAttribute("onclick", "popUserInfo(-1)");
        setTimeout(function(){ viewUsers(document.getElementById("keyword").value.trim()); }, 1000);
    });
    hb.addEventListener("click", function() {
        document.getElementById("data").innerHTML = "";
        document.getElementById("searching-busy").style.visibility = "visible";
        document.getElementById("searching-successful").style.visibility = "hidden";
        current.className = "item";
        current = hb;
        current.className = "active item";
        document.getElementById("new").setAttribute("onclick", "popBoardInfo(-1)");
        setTimeout(function(){ viewBoards(document.getElementById("keyword").value.trim()); }, 1000);
    });
    hp.addEventListener("click", function() {
        document.getElementById("data").innerHTML = "";
        document.getElementById("searching-busy").style.visibility = "visible";
        document.getElementById("searching-successful").style.visibility = "hidden";
        current.className = "item";
        current = hp;
        current.className = "active item";
        document.getElementById("new").setAttribute("onclick", "popPostInfo(-1)");
        setTimeout(function(){ viewPosts(document.getElementById("keyword").value.trim()); }, 1000);
    });
    hc.addEventListener("click", function() {
        document.getElementById("data").innerHTML = "";
        document.getElementById("searching-busy").style.visibility = "visible";
        document.getElementById("searching-successful").style.visibility = "hidden";
        current.className = "item";
        current = hc;
        current.className = "active item";
        document.getElementById("new").setAttribute("onclick", "popCommentInfo(-1)");
        setTimeout(function(){ viewComments(document.getElementById("keyword").value.trim()); }, 1000);
    });
    document.getElementById("search").addEventListener("click", function() {
        current.click();
    });
    /* default */
    document.getElementById("searching-busy").style.visibility = "visible";
    document.getElementById("searching-successful").style.visibility = "hidden";
    current.click();
});
