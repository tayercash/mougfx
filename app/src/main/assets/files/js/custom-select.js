function initCustomSelects() {
    $('select[data-custom-select]:not(.no-custom)').each(function () {
        const $this = $(this);
        if ($this.parent().hasClass('custom-select-wrapper')) return;

        const selectId = $this.attr('id') || Math.random().toString(36).substr(2, 9);
        $this.attr('data-custom-id', selectId);

        const options = $this.find('option');
        const $wrapper = $('<div class="custom-select-wrapper"></div>');
        const $mgSelect = $('<div class="mg-select"></div>');
        const $trigger = $('<div class="custom-select-trigger"></div>');
        const $optionsContainer = $('<div class="custom-options"></div>');

        $this.addClass('select-hidden');
        $this.wrap($wrapper);
        $this.after($mgSelect);

        $optionsContainer.attr('data-for', selectId);
        $('body').append($optionsContainer);

        const $selected = options.filter(':selected');
        const $first = options.first();
        $trigger.html($selected.length ? $selected.text() : $first.text());
        $mgSelect.append($trigger);

        const hasSearch = options.length > 7;
        if (hasSearch) {
            const $searchBox = $('<div class="custom-select-search-box" style="padding:8px 10px;border-bottom:1px solid rgba(255,255,255,0.08);background:#121214;position:sticky;top:0;z-index:10"><input type="text" placeholder="بحث..." class="custom-select-search-input" style="width:100%;padding:6px 10px;background:rgba(255,255,255,0.05);border:1px solid rgba(255,255,255,0.1);border-radius:6px;color:#fff;font-size:0.85rem;text-align:start;outline:none;font-family:inherit;"></div>');
            $optionsContainer.append($searchBox);
            $searchBox.find('input').on('click', function(e) { e.stopPropagation(); })
                .on('input', function(e) {
                    const query = $(this).val().toLowerCase().trim();
                    $optionsContainer.find('.custom-option').each(function() {
                        const text = $(this).text().toLowerCase();
                        if (text.indexOf(query) > -1) { $(this).show(); }
                        else { $(this).hide(); }
                    });
                });
        }

        options.each(function () {
            const $option = $(this);
            const $customOption = $('<div class="custom-option"></div>')
                .text($option.text())
                .attr('data-value', $option.val());
            if ($option.is(':selected')) $customOption.addClass('selection');
            $optionsContainer.append($customOption);
        });

        $this.on('change', function () {
            const val = $this.val();
            const text = $this.find('option:selected').text();
            $trigger.text(text);
            $optionsContainer.find('.custom-option').removeClass('selection')
                .filter('[data-value="' + val.replace(/"/g, '&quot;') + '"]').addClass('selection');
        });

        $trigger.on('click', function (e) {
            e.stopPropagation();
            const isOpen = $mgSelect.hasClass('open');
            $('.mg-select').removeClass('open');
            $('.custom-options').not($optionsContainer).removeClass('open-options');

            if (!isOpen) {
                $mgSelect.addClass('open');
                $optionsContainer.addClass('open-options');
                const $currentSearch = $optionsContainer.find('.custom-select-search-input');
                if ($currentSearch.length > 0) $currentSearch.val('').trigger('input');
                updatePosition();
                if ($currentSearch.length > 0) setTimeout(() => $currentSearch.focus(), 50);
            } else {
                $mgSelect.removeClass('open');
                $optionsContainer.removeClass('open-options');
            }
        });

        function updatePosition() {
            if ($mgSelect.hasClass('open')) {
                const rect = $trigger[0].getBoundingClientRect();
                const windowWidth = window.innerWidth;
                const windowHeight = window.innerHeight;

                $optionsContainer.css({ visibility: 'hidden', display: 'block', width: 'max-content' });
                const dropdownWidth = $optionsContainer.outerWidth();
                const dropdownHeight = $optionsContainer.outerHeight();
                $optionsContainer.css({ visibility: '', display: '' });

                let leftPos = rect.left;
                let rightPos = 'auto';

                // Prefer opening downward; upward if not enough room
                let topPos;
                if (rect.bottom + dropdownHeight > windowHeight && rect.top > dropdownHeight) {
                    topPos = (rect.top - dropdownHeight - 5) + 'px';
                } else {
                    topPos = (rect.bottom + 5) + 'px';
                }

                // Smart horizontal positioning: stay within window edges
                const maxWidth = Math.min(200, windowWidth - 20);
                if (leftPos + maxWidth > windowWidth) {
                    // Would overflow right edge → anchor from right
                    leftPos = 'auto';
                    rightPos = Math.max(5, windowWidth - rect.right) + 'px';
                } else if (leftPos < 0) {
                    leftPos = '5px';
                }

                $optionsContainer.css({
                    position: 'fixed',
                    zIndex: 2147483647,
                    minWidth: rect.width + 'px',
                    width: 'max-content',
                    maxWidth: maxWidth + 'px',
                    left: leftPos,
                    right: rightPos,
                    top: topPos
                });
            }
        }

        let ticking = false;
        function handleScroll() {
            if (!ticking) {
                window.requestAnimationFrame(function () { updatePosition(); ticking = false; });
                ticking = true;
            }
        }

        $(window).on('scroll resize', handleScroll);
        $('.header, .full_view').on('scroll', handleScroll);

        $optionsContainer.on('click', '.custom-option', function (e) {
            e.stopPropagation();
            const val = $(this).attr('data-value');
            const text = $(this).text();
            $this.val(val);
            $this.triggerHandler('change');
            if ($this[0]) {
                $this[0].dispatchEvent(new Event('change', { bubbles: true }));
            }
            $trigger.text(text);
            $(this).addClass('selection').siblings().removeClass('selection');
            $mgSelect.removeClass('open');
            $optionsContainer.removeClass('open-options');
        });
    });
}

$(document).on('click', function () {
    $('.mg-select').removeClass('open');
    $('.custom-options').removeClass('open-options');
});

$(document).ready(function () {
    initCustomSelects();
});
