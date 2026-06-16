var now_navigation_link_index = 0;
$(".navigation ul li.list").click(function () {
    scrolled_from_top = $(document).scrollTop();
    $(".navigation ul li.list").eq(now_navigation_link_index).attr("data-scrolled_from_top", scrolled_from_top);
    old_navigation_link = $(".navigation ul li.list").eq(now_navigation_link_index).find("a").attr("data-full_iframe_target_url");
    $(".navigation ul li.list").removeClass("active");
    $(this).addClass("active");
    now_navigation_link_index = $(".navigation ul li.list").index(this);
    fix_indicator_positon();
    new_navigation_link = $(this).find("a").attr("data-full_iframe_target_url");

    $("[data-navigation_url]").removeClass("show").addClass("hidden");

    nav_header_title = typeof $(this).attr("data-header_title") !== "undefined" ? " - " + $(this).attr("data-header_title") : "";
    $("#extra_title").text(nav_header_title);

    if ($(`[data-navigation_url="${new_navigation_link}"]`).length > 0) {
        $(`[data-navigation_url="${new_navigation_link}"]`).addClass("show").removeClass("hidden");
        if (new_navigation_link == old_navigation_link) {
            load_navigation_html_content(new_navigation_link, "reload");
        } else {
            latest_scrolled_from_top = parseInt($(this).attr("data-scrolled_from_top"));
            $(document).scrollTop(latest_scrolled_from_top);
            resize_text();
        }
    } else {
        load_navigation_html_content(new_navigation_link);
    }



});

function load_navigation_html_content(new_navigation_link, type = "first_load") {
    $(document).ready(function () {
        $.ajax({
            "type": "GET",
            "url": new_navigation_link,
            success: function (res, status, xhr) {
                if (xhr.status == 200) {
                    doc = $(res).wrapAll('<div>').parent();
                    $(doc).find("script[src]").each(function () {
                        source_link = $(this).attr("src");
                        new_source_link = source_link.includes("?") ? source_link + "&" : source_link + "?" + "token=" + Date.now();
                        $(this).attr("src", new_source_link);
                    });

                    new_html = $(`<div data-navigation_url="${new_navigation_link}">${res}</div>`);

                    if (type == "reload") {
                        $(`[data-navigation_url="${new_navigation_link}"]`).remove();
                    }

                    if ($(new_html).find("#header_actions").length > 0) {
                        action_btns = $(new_html).find("#header_actions").html();
                        $("#header_actions").append(`<div data-navigation_url="${new_navigation_link}">` + action_btns + `<div>`);
                        $(new_html).find("#header_actions").remove();
                    }
                    $("#content").append(new_html);
                    now_active_panal = $(".navigation ul li.list.active").index();
                    what_window["back_buttons_functions_" + what_window.now_active_panal] = [];

                    readypopups();
                    resize_text();
                    apply_translations();
                    if (typeof initCustomSelects === 'function') initCustomSelects();
                }
            }
        });
    });


}

function fix_indicator_positon(animation = true) {
    var indicator = document.querySelector(".navigation .indicator");
    var links = document.querySelectorAll(".navigation ul li.list");
    if (!indicator || !links.length) return;
    var activeIdx = -1;
    for (var i = 0; i < links.length; i++) {
        if (links[i].classList.contains("active")) { activeIdx = i; break; }
    }
    if (activeIdx < 0) return;

    var navW = links[0].offsetWidth;
    var indW = indicator.offsetWidth;
    if (!navW || !indW) return;

    // Keep indicator width in sync with nav item count
    var pct = 100 / links.length;
    indicator.style.width = pct + "%";

    var isRtl = document.documentElement.getAttribute("dir") === "rtl";
    var base = (navW - indW) / 2 + activeIdx * navW;
    var offset = isRtl ? -base : base;

    if (animation) {
        indicator.classList.add("animation");
    } else {
        indicator.classList.remove("animation");
    }

    indicator.style.transform = "translateX(" + offset + "px)";
}
fix_indicator_positon(false);
window.onresize = function (event) {
    fix_indicator_positon(false);
};

$(".navigation ul li.active").eq(0).click();