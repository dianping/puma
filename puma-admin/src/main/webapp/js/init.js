$(function() {

  $(".validation-input").on('change', function() {
    var div  = $(this).parent();
    var span = $(this).next();
    if (span.size() === 0) {
      $(this).after("<span></span>");
      span = $(this).next();
    }

    var input  = $(this).val();
    var format = $(this).attr('format');

    var regexp;
    switch(format) {
      case 'server-name':
        regexp = /^\w{1,30}$/;
        break;

      case 'host':
        regexp = /^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/;
        break;

      case 'port':
        regexp = /^[0-9]{1,5}$/;
        break;

      case 'bin-log-file':
        regexp = /^[\w\-. ]+$/;
        break;

      case 'bin-log-position':
        regexp = /^[0-9]{1,20}$/;
        break;

      case 'day':
        regexp = /^[1-9]{1,2}$/;
        break;

      default: alert("regexp error");
    }

    if (regexp.test(input)) {
      util.validate.success(div, span);
    } else {
      util.validate.error(div, span);
    }
  });
});