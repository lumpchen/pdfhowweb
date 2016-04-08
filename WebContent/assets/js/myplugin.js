
$(function () {
    $.fn.my = function () {
        var defaults = {
            Event: "click",
            msg: "Hello!"
            
        };
        var options = $.extend(defaults, options);
        $(this).on(options.Event, this, function (e) {
            alert(options.msg);
        });
    };
});