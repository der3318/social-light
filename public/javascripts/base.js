/* background */
document.body.style.backgroundColor = "black";
document.body.style.background = "url('/images/background.jpg') repeat-y center center";
document.body.style.backgroundSize = "cover";

/* foreground */
document.body.style.color = "white";

/* opacity */
document.getElementById("content").style.opacity = 0.8;
document.getElementById("footer").style.opacity = 0.8;

/* fonts */
document.body.style.fontFamily = "Comic Sans MS, Microsoft JhengHei";

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
