/* view users */
function viewUsers(search) {
    connectAndUpdate("/api/v1/admin/users", "POST", {"search": search}, rsp => {
        var keyIcon = ' <img src="/icons/key.svg" style="filter:invert(100%)"/> ';
        var div = document.getElementById("data");
        rsp.data.forEach(e => {
            var row = div.insertRow(div.rows.length);
            row.insertCell(0).innerHTML = e.id;
            row.insertCell(1).innerHTML = e.name + keyIcon + e.account + "/" + e.password;
            row.innerHTML += '<td class="selectable"><a href="#' + e.id + '"><i class="edit outline icon"></i></a></td>';
        });
        document.getElementById("searching-busy").style.visibility = "hidden";
        document.getElementById("searching-successful").style.visibility = "visible";
        $('#searching-successful').progress({autoSuccess: false, value: rsp.data.length, total: rsp.total, text: {percent: 'showing {value} of {total}'}});
    });
}

/* view boards */
function viewBoards(search) {
    connectAndUpdate("/api/v1/admin/boards", "POST", {"search": search}, rsp => {
        var div = document.getElementById("data");
        rsp.data.forEach(e => {
            var row = div.insertRow(div.rows.length);
            row.insertCell(0).innerHTML = e.id;
            row.insertCell(1).innerHTML = e.name;
            row.innerHTML += '<td class="selectable"><a href="#' + e.id + '"><i class="edit outline icon"></i></a></td>';
        });
        document.getElementById("searching-busy").style.visibility = "hidden";
        document.getElementById("searching-successful").style.visibility = "visible";
        $('#searching-successful').progress({autoSuccess: false, value: rsp.data.length, total: rsp.total, text: {percent: 'showing {value} of {total}'}});
    });
}

/* view posts */
function viewPosts(search) {
    connectAndUpdate("/api/v1/admin/posts", "POST", {"search": search}, rsp =>  {
        var calendarIcon = ' <img src="/icons/calendar.svg" style="filter:invert(100%)"/> ';
        var div = document.getElementById("data");
        rsp.data.forEach(e => {
            var row = div.insertRow(div.rows.length);
            row.insertCell(0).innerHTML = e.id;
            row.insertCell(1).innerHTML = e.title + " (" + e.content.substring(0, 5) + "...)" + calendarIcon + e.ts_create;
            row.innerHTML += '<td class="selectable"><a href="#' + e.id + '"><i class="edit outline icon"></i></a></td>';
        });
        document.getElementById("searching-busy").style.visibility = "hidden";
        document.getElementById("searching-successful").style.visibility = "visible";
        $('#searching-successful').progress({autoSuccess: false, value: rsp.data.length, total: rsp.total, text: {percent: 'showing {value} of {total}'}});
    });
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
        setTimeout(function(){ viewUsers(document.getElementById("keyword").value.trim()); }, 1000);
    });
    hb.addEventListener("click", function() {
        document.getElementById("data").innerHTML = "";
        document.getElementById("searching-busy").style.visibility = "visible";
        document.getElementById("searching-successful").style.visibility = "hidden";
        current.className = "item";
        current = hb;
        current.className = "active item";
        setTimeout(function(){ viewBoards(document.getElementById("keyword").value.trim()); }, 1000);
    });
    hp.addEventListener("click", function() {
        document.getElementById("data").innerHTML = "";
        document.getElementById("searching-busy").style.visibility = "visible";
        document.getElementById("searching-successful").style.visibility = "hidden";
        current.className = "item";
        current = hp;
        current.className = "active item";
        setTimeout(function(){ viewPosts(document.getElementById("keyword").value.trim()); }, 1000);

    });
    document.getElementById("search").addEventListener("click", function() {
        current.click();
    });
    /* default */
    document.getElementById("searching-busy").style.visibility = "visible";
    document.getElementById("searching-successful").style.visibility = "hidden";
    current.click();
});
