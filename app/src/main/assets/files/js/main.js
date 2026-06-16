function getQueryVariable(variable, meth = 1, link = "") {
    if (meth == 1) {
        var query = window.location.search.substring(1);
    } else {
        var query = link.split("?")[1];
    }
    var vars = query.split("&");
    for (var i = 0; i < vars.length; i++) {
        var pair = vars[i].split("=");
        if (pair[0] == variable) {
            return pair[1];
        }
    }
    return (false);
}

window.addEventListener('resize', resize_text, true);
function resize_text() {
    var text_class = "mou_resize_text";
    var text_size = 8;
    elmnt = $("." + text_class);

    $(elmnt).each(function () {
        text_size = parseInt($(this).attr("data-textsize"));
        elmnt_width = $(this).parent().width();
        new_text_size = text_size / 100 * elmnt_width;
        $(this)[0].style.setProperty('font-size', new_text_size + "px", 'important');
    });

}
Array.prototype.Unshift = function (val) {
    now_active_panal = $(what_window.document).find(".navigation ul li.list.active").index();
    if (typeof what_window["back_buttons_functions_" + what_window.now_active_panal] == "undefined") {
        what_window["back_buttons_functions_" + what_window.now_active_panal] = [];
    }
    what_window["back_buttons_functions_" + what_window.now_active_panal].unshift(val);
    // this.unshift(val);
}

var close_app_now = false;
function back_button_clicked(index = false) {
    if (index == true) {

    } else {

        now_active_panal = $(".navigation ul li.list.active").index();

        if (typeof what_window["back_buttons_functions_" + now_active_panal] !== "undefined" && what_window["back_buttons_functions_" + now_active_panal].length > 0) {
            if (typeof what_window["back_buttons_functions_" + now_active_panal][0] == "function") {
                what_window["back_buttons_functions_" + now_active_panal][0]();
            }
            what_window["back_buttons_functions_" + now_active_panal].shift();
        } else {

            if (close_app_now == true) {
                mouscripts.exit_app();
            } else {
                mouscripts.showToast("Press Back Button Again To Exit App.");
                close_app_now = true;
                close_app_now_time_out = setTimeout(function () {
                    close_app_now = false;
                }, 1000 * 2);
            }
        }
    }
}