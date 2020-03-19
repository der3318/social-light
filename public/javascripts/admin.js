/* [test] get data via api */
connectAndUpdate("/api/v1/posts", "POST", {"policy": 1}, rsp => {
    var div = document.getElementById("records");
    div.innerHTML = "";
    rsp.posts.forEach(element => {
        var id = element.id;
        var description = "[" + element.title + "] " + element.content;
        var row = div.insertRow(records.rows.length);
        row.insertCell(0).innerHTML = id;
        row.insertCell(1).innerHTML = description.substring(0, 20);
        row.innerHTML += '<td class="selectable"><a href="#' + id + '"><i class="edit outline icon"></i></a></td>';
    });
});
