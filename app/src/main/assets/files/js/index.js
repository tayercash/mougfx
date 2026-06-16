var what_window = window;
if (window.frameElement) {
    what_window = window.parent;
}
what_window.e_m = false;

function set_e_m(e_m = what_window.e_m) {
    what_window.e_m = e_m;

    if (what_window.e_m == true) {
        $(".remove_ads").removeAttr("disabled").find(".btn_text").text("Remove Ads");
        $(".pro_status").hide();
    } else {
        $(".remove_ads").attr("disabled","disabled").find(".btn_text").text("Ads Removed.");
        $(".pro_status").show();
    }
}