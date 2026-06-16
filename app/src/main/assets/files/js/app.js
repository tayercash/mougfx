var file_props = {};
var decoded_UserCustomIni = {};
var current_lang = 'en';

var translations = {
    ar: {
        nav_gfx: 'GFX',
        nav_advanced: 'متقدم',
        nav_resolution: 'الدقة',
        nav_info: 'معلومات',
        editor_title: 'المحرر المتقدم',
        editor_desc: 'تعديل جميع خصائص ملف active.sav',
        editor_save: 'حفظ التغييرات',
        editor_reload: 'إعادة تحميل',
        editor_no_file: 'لم يتم تحميل ملف بعد.',
        resolution: 'الدقة',
        resolution_desc: 'ضبط الدقة المثلى للعبة',
        graphics: 'الجودة',
        graphics_desc: 'اختر أفضل جودة لجهازك',
        lobby_graphics: 'جودة اللوبي',
        lobby_graphics_desc: 'اختر أفضل جودة للوبي',
        fps: 'معدل الإطارات',
        fps_desc: 'رفع حد FPS الأقصى',
        styles: 'الألوان',
        styles_desc: 'اختر فلتر الألوان المفضل',
        apply_perspective: 'تطبيق المنظور',
        reset_resolution: 'إعادة الدقة',
        apply_settings: 'تطبيق',
        apply: 'تطبيق',
        close_perspective: 'إغلاق المنظور',
        width: 'العرض',
        height: 'الارتفاع',
        dpi: 'DPI',
        reset_perspective: 'إعادة المنظور',
        rate_app: 'تقييم التطبيق',
        share_app: 'مشاركة التطبيق',
        reloaded: 'تم إعادة التحميل',
        nav_settings: 'الإعدادات',
        settings_title: 'الإعدادات',
        settings_language: 'اللغة',
        settings_language_desc: 'اختر لغة التطبيق',
        settings_version: 'إصدار التطبيق',
        settings_version_desc: 'معلومات إصدار Mou GFX',
        settings_autoload: 'تحميل تلقائي',
        settings_autoload_desc: 'تحميل active.sav تلقائياً عند البدء',
        settings_access_method: 'طريقة الوصول',
        settings_access_method_desc: 'اختيار طريقة الوصول للملفات',
        access_auto: 'تلقائي',
        access_root: 'Root',
        access_shizuku: 'Shizuku',
        access_direct: 'مباشر',
        settings_launch_game: 'تشغيل اللعبة',
        settings_launch_game_desc: 'فتح PUBG Mobile مباشرة',
        settings_launch_btn: 'تشغيل',
        settings_reset_all: 'إعادة ضبط الإعدادات',
        settings_reset_confirm: 'هل أنت متأكد من إعادة ضبط جميع الإعدادات؟',
        settings_reset_done: 'تم إعادة ضبط الإعدادات',
        settings_share_title: 'جرب Mou GFX',
        settings_share_text: 'جرب Mou GFX\n\nحمّله الآن مجاناً',
        overlay_required: 'صلاحية الظهور فوق التطبيقات مطلوبة',
        overlay_desc: 'يحتاج التطبيق صلاحية "الظهور فوق التطبيقات" لعرض لوحة المنظور العائمة. من فضلك فعّل الخاصية من الإعدادات.',
        open_overlay_settings: 'فتح الإعدادات',
        apply_and_launch: 'تطبيق وفتح اللعبة',
        info_open: 'معلومات',
        info_desc: 'معلومات التطبيق والتقييم',
        info_tagline: 'تطبيق تعديل رسومات ببجي موبايل',
        app_version_label: 'الإصدار',
        settings_countdown: 'مدة العداد',
        settings_countdown_desc: 'مدة العد التنازلي للمنظور (بالثواني)',
        settings_countdown_disable: 'إيقاف العداد',
        settings_countdown_disable_desc: 'إلغاء العد التنازلي التلقائي (إعادة يدوية)',
        countdown_3s: '3 ثواني',
        countdown_5s: '5 ثواني',
        countdown_10s: '10 ثواني',
        countdown_15s: '15 ثواني',
        countdown_30s: '30 ثواني',
        info_title: 'معلومات',
    },
    en: {
        nav_gfx: 'GFX',
        nav_advanced: 'Advanced',
        nav_resolution: 'Resolution',
        nav_info: 'Info',
        editor_title: 'Advanced Editor',
        editor_desc: 'Edit all properties of active.sav',
        editor_save: 'Save All Changes',
        editor_reload: 'Reload',
        editor_no_file: 'No file loaded yet.',
        resolution: 'Resolution',
        resolution_desc: 'Set optimal resolution for the game',
        graphics: 'Graphics',
        graphics_desc: 'Choose the best graphics for your device',
        lobby_graphics: 'Lobby Graphics',
        lobby_graphics_desc: 'Choose the best lobby graphics',
        fps: 'FPS',
        fps_desc: 'Unlock Maximum FPS limit',
        styles: 'Styles',
        styles_desc: 'Choose your favorite color filter',
        apply_perspective: 'Apply Perspective',
        reset_resolution: 'Reset Resolution',
        apply_settings: 'Apply',
        apply: 'Apply',
        close_perspective: 'Close Perspective',
        width: 'Width',
        height: 'Height',
        dpi: 'DPI',
        reset_perspective: 'Reset Perspective',
        rate_app: 'Rate App',
        share_app: 'Share App',
        reloaded: 'Reloaded',
        nav_settings: 'Settings',
        settings_title: 'Settings',
        settings_language: 'Language',
        settings_language_desc: 'Select app language',
        settings_version: 'App Version',
        settings_version_desc: 'Mou GFX version info',
        settings_autoload: 'Auto Load',
        settings_autoload_desc: 'Auto-load active.sav on start',
        settings_access_method: 'Access Method',
        settings_access_method_desc: 'Choose file access method',
        access_auto: 'Auto',
        access_root: 'Root',
        access_shizuku: 'Shizuku',
        access_direct: 'Direct',
        settings_launch_game: 'Launch Game',
        settings_launch_game_desc: 'Open PUBG Mobile directly',
        settings_launch_btn: 'Launch',
        settings_reset_all: 'Reset All Settings',
        settings_reset_confirm: 'Are you sure you want to reset all settings?',
        settings_reset_done: 'Settings reset',
        settings_share_title: 'Try Mou GFX',
        settings_share_text: 'Try Mou GFX\n\nDownload Now For Free',
        overlay_required: 'Overlay permission required',
        overlay_desc: 'The app needs "Draw over other apps" permission to show the floating perspective panel. Please enable it in Settings.',
        open_overlay_settings: 'Open Settings',
        apply_and_launch: 'Apply & Launch',
        info_open: 'Info',
        info_desc: 'App info and rate us',
        info_tagline: 'PUBG Mobile graphics tweaker app',
        app_version_label: 'Version',
        settings_countdown: 'Countdown Duration',
        settings_countdown_desc: 'Countdown duration for perspective reset (seconds)',
        settings_countdown_disable: 'Disable Countdown',
        settings_countdown_disable_desc: 'Turn off auto-reset countdown (manual only)',
        countdown_3s: '3 seconds',
        countdown_5s: '5 seconds',
        countdown_10s: '10 seconds',
        countdown_15s: '15 seconds',
        countdown_30s: '30 seconds',
        info_title: 'Info',
    }
};

function __(key) {
    if (translations[current_lang] && translations[current_lang][key]) {
        return translations[current_lang][key];
    }
    if (translations.en && translations.en[key]) {
        return translations.en[key];
    }
    return key;
}

function apply_translations() {
    $('[data-i18n]').each(function () {
        var key = $(this).attr('data-i18n');
        $(this).text(__(key));
    });
}

$(document).on('input', 'input[type=number], input[type=text], .editor-input, .resolution_select', function () {
    var start = this.selectionStart;
    this.value = this.value.replace(/[\u0660-\u0669\u06F0-\u06F9]/g, function (ch) {
        return String.fromCharCode(ch.charCodeAt(0) & 0x0F);
    });
    this.setSelectionRange(start, start);
});

$(document).ready(function () {
    if (typeof mouscripts !== "undefined") {
        if (mouscripts.is_network_available()) {
            mouscripts.InitMobileAds();
        } else {
            window.addEventListener("online", (event) => {
                mouscripts.InitMobileAds();
            });
        }

        app_language = mouscripts.get_sys_lang();
        set_app_language(app_language);

    } else {
        set_app_language("en");
    }
});
function switchLanguage(lang) {
    current_lang = lang;
    try { localStorage.setItem('app_lang', lang); } catch(e) {}
    if (lang == "ar") {
        $("html").attr("dir", "rtl");
    } else {
        $("html").attr("dir", "ltr");
    }
    $('.navigation ul li a .text').each(function () {
        var link = $(this).closest('a').attr('data-full_iframe_target_url');
        if (link === 'gfx.html') $(this).text(__('nav_gfx'));
        if (link === 'resolution.html') $(this).text(__('nav_resolution'));
        if (link === 'editor.html') $(this).text(__('nav_advanced'));
        if (link === 'settings.html') $(this).text(__('nav_settings'));
        if (link === 'info.html') $(this).text(__('nav_info'));
    });
    fix_indicator_positon(false);
    apply_translations();
    // Refresh all custom-select triggers with new translations
    $('select[data-custom-select]').each(function () {
        var $trigger = $(this).parent('.custom-select-wrapper').find('.custom-select-trigger');
        if ($trigger.length) {
            var $selOpt = $(this).find('option:selected');
            $trigger.text($selOpt.length ? $selOpt.text() : $(this).find('option').first().text());
        }
    });
    // Update the language select value + custom trigger if settings page is loaded
    var $sel = $('#lang_select');
    if ($sel.length) {
        $sel.val(lang);
        var $trigger = $sel.parent('.custom-select-wrapper').find('.custom-select-trigger');
        if ($trigger.length) $trigger.text($sel.find('option:selected').text());
    }
}
function set_app_language(app_language) {
    var saved = null;
    try { saved = localStorage.getItem('app_lang'); } catch(e) {}
    var lang = saved || app_language || 'en';
    switchLanguage(lang);
}


function load_settings_page() {

    for (i = 0; i < full_normal_cats.length; i++) {
        check_box = $(`
            <label class="checkbox_container">${full_normal_cats[i]}
                <input type="checkbox" data-cat_setting="${full_normal_cats[i]}">
                <span class="checkmark"></span>
            </label>
        `);
        if (user_normal_cats.includes(full_normal_cats[i])) {
            $(check_box).find("input").prop('checked', true);
        }
        $("#normal_cats_setting").append(check_box);
    }

    for (i = 0; i < full_float_cats.length; i++) {
        check_box = $(`
            <label class="checkbox_container">${full_float_cats[i]}
                <input type="checkbox" data-cat_setting="${full_float_cats[i]}">
                <span class="checkmark"></span>
            </label>
        `);
        if (user_float_cats.includes(full_float_cats[i])) {
            $(check_box).find("input").prop('checked', true);
        }
        $("#float_cats_setting").append(check_box);
    }

}
function load_counter_page() {
}

function load_file_props(props) {

    let binaryString = atob(props);

    // إذا كان ملف الـ SAV يحتوي على نص JSON بداخله:
    let decodedData = decodeURIComponent(escape(binaryString));
    let file_props = JSON.parse(decodedData);


    update_ui(file_props);
}

function update_ui(props) {

    $('#graphics').val(props.BattleRenderQuality).change();
    $('#lobby_graphics').val(props.LobbyRenderQuality).change();
    $('#fps').val(props.BattleFPS).change();
    $('#style').val(props.BattleRenderStyle).change();


}

function copyText(Text) {
    navigator.clipboard.writeText(Text).then(() => {
        console.log("Text copied to clipboard");

    }).catch(err => {
        console.error("Failed to copy: ", err);
    });
}

function apply_settings() {
    file_props = {
        "LobbyRenderQuality": parseInt($('#lobby_graphics').val()),
        "BattleRenderQuality": parseInt($('#graphics').val()),
        "BattleFPS": parseInt($('#fps').val()),
        "LobbyFPS": parseInt($('#fps').val()),
        "BattleRenderStyle": parseInt($('#style').val()),
        "LobbyRenderStyle": parseInt($('#style').val())
    };


//    file_props = {
//         "LobbyRenderQuality": parseInt($('#graphics').val()),
//         "BattleRenderQuality": parseInt($('#graphics').val()),
//         "BattleFPS": parseInt($('#fps').val()),
//         "LobbyFPS": parseInt($('#fps').val()),
//         "BattleRenderStyle": parseInt($('#style').val()),
//         "LobbyRenderStyle": parseInt($('#style').val()),
//         "GFBestQBattle": 1,
//         "GraphicFavor": 4,
//         "bEnergySaveManuelChangeFlag2": false,
//         "FPSLevel": 6,
//         "HitEffectColor": 2,
//         "HurtEffectColor": false,
//         "OBSHitFeedback": false

//     };

    // decoded_UserCustomIni["BackUp DeviceProfile"]["r.UserQualitySetting"] = 1;
    // decoded_UserCustomIni["UserCustom DeviceProfile"]["r.UserQualitySetting"] = 1;

    const section = "UserCustom DeviceProfile";
    const backup_section = "BackUp DeviceProfile";
    // تطبيق قيم الظل العالية

    // decoded_UserCustomIni[backup_section]["r.UserShadowSwitch"] = 1;
    // decoded_UserCustomIni[backup_section]["r.ShadowQuality"] = 3;
    // decoded_UserCustomIni[backup_section]["r.Shadow.MaxCSMResolution"] = 1024;
    // decoded_UserCustomIni[backup_section]["r.Mobile.DynamicObjectShadow"] = 1;

    // decoded_UserCustomIni[section]["r.UserShadowSwitch"] = 1;
    // decoded_UserCustomIni[section]["r.ShadowQuality"] = 3;
    // decoded_UserCustomIni[section]["r.Shadow.MaxCSMResolution"] = 1024;
    // decoded_UserCustomIni[section]["r.Mobile.DynamicObjectShadow"] = 1;

    console.log(JSON.stringify(file_props));
    // console.log(JSON.stringify(decoded_UserCustomIni));

    if (typeof mouscripts !== "undefined") {
        // save_ini_data();

        mouscripts.apply_file_props(JSON.stringify(file_props));

        // setTimeout(() => {
        //     window.mouscripts.launchGame();
        // }, 1500);
    }
}

const XOR_KEY = 121; // القيمة المقابلة لـ 0x79

const UE4Editor = {
    // تشفير نص عادي إلى Hex مشفر
    encrypt: (text) => {
        return text.split('')
            .map(char => {
                let encrypted = char.charCodeAt(0) ^ XOR_KEY;
                return encrypted.toString(16).toUpperCase().padStart(2, '0');
            })
            .join('');
    },

    // فك تشفير Hex مشفر إلى نص عادي
    decrypt: (hex) => {
        let str = '';
        for (let i = 0; i < hex.length; i += 2) {
            let encryptedValue = parseInt(hex.substr(i, 2), 16);
            str += String.fromCharCode(encryptedValue ^ XOR_KEY);
        }
        return str;
    }
};
function handleIniData(base64Data) {
    let inidata = decodeURIComponent(escape(atob(base64Data)));
    let lines = inidata.split('\n');
    let decryptedContent = "";
    const result = {};
    lines.forEach(line => {
        // التحقق مما إذا كان السطر يحتوي على كود مشفر (يبدأ بـ +CVars=)
        if (line.startsWith("+CVars=")) {
            let encryptedHex = line.replace("+CVars=", "").trim();
            // console.log("فك التشفير للسطر: " + UE4Editor.decrypt(encryptedHex));
            decryptedContent += "+CVars=" + UE4Editor.decrypt(encryptedHex) + "\n";
        } else {
            // الأسطر العادية (مثل العناوين بين الأقواس) تظل كما هي
            decryptedContent += line + "\n";
        }
    });
    // console.log("تم فك التشفير بنجاح:");
    decoded_UserCustomIni = iniToJSON(decryptedContent);
    alert(JSON.stringify(decoded_UserCustomIni));

    // copyText(JSON.stringify(decoded_UserCustomIni));

    // copyText(JSON.stringify(decoded_UserCustomIni));

    // custom_ini = encode_UserCustomIni(JsonToIni(decoded_UserCustomIni));

    // save_ini_data();
}

function save_ini_data() {
    // 3. التحويل لـ Base64 والإرسال
    try {

        encoded_UserCustomIni = encode_UserCustomIni(JsonToIni(decoded_UserCustomIni));

        // copyText(encoded_UserCustomIni);
        // alert(encoded_UserCustomIni);

        let base64Output = btoa(unescape(encodeURIComponent(encoded_UserCustomIni)));

        if (window.mouscripts && window.mouscripts.saveIniFile) {
            window.mouscripts.saveIniFile(base64Output);
        }
    } catch (e) {
        console.error("Error:", e);
    }
}

function iniToJSON(iniString) {
    const lines = iniString.split('\n');
    const result = {};
    let currentSection = "";

    lines.forEach(line => {
        line = line.trim();
        if (!line) return;

        if (line.startsWith('[') && line.endsWith(']')) {
            currentSection = line.substring(1, line.length - 1);
            result[currentSection] = {};
        }
        else if (line.startsWith('+CVars=')) {
            let content = line.replace('+CVars=', '');
            let firstEq = content.indexOf('='); // نستخدم index أول = لضمان الدقة
            let key = content.substring(0, firstEq);
            let value = content.substring(firstEq + 1);

            if (key && value !== undefined) {
                if (value.includes('.')) {
                    result[currentSection][key] = value;
                } else {
                    let numValue = parseInt(value);
                    result[currentSection][key] = isNaN(numValue) ? value : numValue;
                }
            }
        }
    });
    return result;
}

function apply_settings_and_launch() {
    apply_settings();
    setTimeout(function() {
        if (typeof mouscripts !== "undefined") mouscripts.launchGame();
    }, 500);
}

function save_advanced_and_launch() {
    save_advanced();
    setTimeout(function() {
        if (typeof mouscripts !== "undefined") mouscripts.launchGame();
    }, 500);
}
function JsonToIni(jsonObject) {
    let iniString = "";

    for (const section in jsonObject) {
        // 1. إضافة اسم القسم بين أقواس [Section]
        iniString += `[${section}]\n`;

        const variables = jsonObject[section];
        for (const key in variables) {
            let value = variables[key];

            // 2. معالجة القيم: إذا كان الرقم 0 يفضل بقاؤه 0، وإذا كان كسر يظل كما هو
            // تحويل القيمة لنص لإضافتها
            iniString += `+CVars=${key}=${value}\n`;
        }

        // 3. إضافة سطر فارغ بين الأقسام للتنظيم
        iniString += "\n";
    }

    // تنظيف المسافات الزائدة في نهاية الملف
    return iniString.trim() + "\n\n\n";
}

function encode_UserCustomIni(plainText) {
    // تقسيم النص مع الحفاظ على الأسطر حتى لو فارغة
    let lines = plainText.split('\n');
    let encodedLines = [];

    lines.forEach(line => {
        // نستخدم trim فقط للتحقق، لكننا نعالج السطر الأصلي
        let checkLine = line.trim();

        if (checkLine.startsWith("+CVars=")) {
            // استخراج ما بعد العلامة مباشرة دون حذف مسافات قد تكون مقصودة
            let command = checkLine.substring(7); // "+CVars=".length هو 7

            // تشفير الأمر وإضافته للمصفوفة
            encodedLines.push("+CVars=" + UE4Editor.encrypt(command));
        } else {
            // أي سطر آخر (عناوين أو أسطر فارغة) يضاف كما هو
            encodedLines.push(line);
        }
    });

    // دمج المصفوفة مرة أخرى باستخدام \n الحقيقية
    let encodedResult = encodedLines.join('\n');

    // 2. السر هنا: حذف أي أسطر فارغة (\n) من نهاية النص بالكامل
    encodedResult = encodedResult.trimEnd();

    // 3. إضافة 3 أسطر فارغة بالضبط (3 مرات \n)
    // ملحوظة: \n واحدة تنهي السطر الحالي، والاثنتان الإضافيتان تصنعان الفراغ
    encodedResult += '\n\n\n';

    return encodedResult;
}

function apply_resolution() {
    if (typeof mouscripts !== "undefined") {
        // 1. الحصول على القيمة المختارة من القائمة
        const selectElement = document.getElementById('resolution_select') || document.querySelector('.resolution_select');
        const selectedValue = selectElement.value;

        if (selectedValue === "Default") {
            // إذا اختار Default نعيد الأبعاد للأصل فوراً
            reset_reselution();
            return;
        }

        // 2. تقسيم النص للحصول على العرض والطول
        // القيمة "1440x1080" تصبح مصفوفة [1440, 1080]
        const dimensions = selectedValue.split('x');
        const width = parseInt(dimensions[0]);
        const height = parseInt(dimensions[1]);
        // كثافة بكسلات مقترحة (يمكنك تعديلها حسب اختيارك)
        const dpi = 320;

        // 3. استدعاء الدالة الجديدة في الجافا (تغيير -> فتح لعبة -> ريسيت)
        mouscripts.applyResLaunchAndReset(width, height, dpi);
    }
}
function reset_reselution() {
    if (typeof mouscripts !== "undefined") {

        mouscripts.reset_phone_reselution();
    }
}

function apply_perspective_from_popup() {
    var w = parseInt(document.getElementById('popup_persp_width').value);
    var h = parseInt(document.getElementById('popup_persp_height').value);
    var dpi = parseInt(document.getElementById('popup_persp_dpi').value);
    if (isNaN(w) || isNaN(h) || isNaN(dpi)) return;
    if (typeof mouscripts !== "undefined") {
        mouscripts.applyResolution(w, h, dpi);
    }
    $('#resolution_popup').closepopup();
}

function reset_perspective_and_close() {
    if (typeof mouscripts !== "undefined") {
        mouscripts.hideAndResetPanel();
    }
    $('#resolution_popup').closepopup();
}

$(document).on('click', '[data-openpopup="resolution_popup"]', function () {
    var sel = document.getElementById('resolution_select');
    if (sel) {
        var val = sel.value;
        if (val && val !== 'Default') {
            var parts = val.split('x');
            if (parts.length === 2) {
                document.getElementById('popup_persp_width').value = parts[0];
                document.getElementById('popup_persp_height').value = parts[1];
            }
        }
    }
});

function RateApp() {
    if (typeof mouscripts !== "undefined") {
        mouscripts.open_external_link("https://play.google.com/store/apps/details?id=" + mouscripts.GetPackageName());
    }
}
function ShareApp() {
    if (typeof mouscripts !== "undefined") {
        mouscripts.share_text_to_apps(__('rate_app'), __('settings_share_text'));
    }
}