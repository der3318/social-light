/* truncate evert input field in div */
function truncateEveryInputFieldInDiv(id) {
    Array.from(document.getElementById(id).getElementsByTagName("input")).forEach(e => {
        e.value = "";
    });
    Array.from(document.getElementById(id).getElementsByTagName("textarea")).forEach(e => {
        e.innerHTML = "";
    });
}
