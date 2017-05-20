$(document).ready(function () {
    var turns = $("turn");
    var context = $("context");

    // create popup corresponding for each turn
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

    // create computed data to put after context
    context.append("<divider/>");
    context.append("<block><bold>Total actions : </bold>" + turns.length + "</block>");
    context.append("<block><bold>Money spent : </bold>" + sum() + "</block>");
    var actions = ["echo", "scan", "heading", "fly", "glimpse", "explore", "scout", "move_to", "exploit", "transform"];
    for (var i in actions) {
        context.append("<" + actions[i] + "><bold>" + actions[i] + "</bold> : " + count(actions[i]) + "</" + actions[i] + ">");
    }
});

/**
 * counts the occurrences of a specified action
 *
 * @param str the action to count
 * @returns {jQuery} count of the specified action
 */
function count(str) {
    return $("action[type=" + str + "]").length;
}

/**
 * Realize the sum of all costs
 * @returns {number} the total cost
 */
function sum() {
    var total = 0;
    $("cost").each(function (d) {
        total += parseInt($(this).text());
    });
    return total;
}
