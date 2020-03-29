/* truncate evert input field in div */
function truncateEveryInputFieldInDiv(id) {
    Array.from(document.getElementById(id).getElementsByTagName("input")).forEach(e => {
        e.value = "";
    });
    Array.from(document.getElementById(id).getElementsByTagName("textarea")).forEach(e => {
        e.value = "";
    });
}

/* check if any input space is empty */
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
