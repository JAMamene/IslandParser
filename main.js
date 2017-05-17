$(document).ready(function () {
    var turns = $("turn");
    turns.click(function () {
        var index = $("turn").index(this) + 1;
        var content = $(this).contents();
        $("<placeholder/>")
            .append($("<popup/>")
                .append("<block-title>Action #" + index + "</block-title> <turn>" + $(this).html() + "</turn>")
            ).appendTo("log");
        $("placeholder").click(function () {
            $(this).remove();
        })
    });
    $("context").append("<block><bold>Total actions : </bold>" + turns.length + "</block>");


    var actions = ["echo", "scan", "heading", "fly", "glimpse", "explore", "scout", "move_to", "exploit", "transform"];
    for (var i in actions) {
        $("context").append("<" + actions[i] + "><bold>" + actions[i] + "</bold> : " + count(actions[i]) + "</" + actions[i] + ">");
    }
});

function count(str) {
    return $("action[type=" + str + "]").length;
}
