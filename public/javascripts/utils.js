/* api with json */
function connectAndUpdate(url, verb, request, action) {
    var xhr = new XMLHttpRequest();
    xhr.open(verb, url, true);
    xhr.setRequestHeader("content-type", "application/json;charset=UTF-8");
    xhr.onreadystatechange = function() {
        if (xhr.readyState == 4) {
            var response = JSON.parse(xhr.responseText);
            console.log(response);
            action(response);
        }
    }
    xhr.send(JSON.stringify(request));
}

/* trigger download */
function triggerDownload(filename, link) {
    var element = document.createElement("a");
    element.setAttribute("href", link);
    element.setAttribute("download", filename);
    element.style.display = "none";
    document.body.appendChild(element);
    element.click();
    document.body.removeChild(element);
}

/* truncate evert input field in div */
function truncateEveryInputFieldInDiv(id) {
    Array.from(document.getElementById(id).getElementsByTagName("input")).forEach(e => {
        e.value = "";
    });
    Array.from(document.getElementById(id).getElementsByTagName("textarea")).forEach(e => {
        e.value = "";
    });
}

/* check if any input field is empty */
function isAnyInputFieldEmptyInDiv(id) {
    var checkedResult = false;
    Array.from(document.getElementById(id).getElementsByTagName("input")).forEach(e => {
        if (!e.readOnly && !e.value.trim()) {
            checkedResult = true;
        }
    });
    Array.from(document.getElementById(id).getElementsByTagName("textarea")).forEach(e => {
        if (!e.readOnly && !e.value.trim()) {
            checkedResult = true;
        }
    });
    return checkedResult;
}
