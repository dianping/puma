$(function() {

  // Validation when input.
  $(".validation-input").on('change', function() {
    validate($(this));
  }).trigger('change');

  // Validation when submit.
  $(".validation-button").on('click', function(event) {
    var isAllValid = true;

    $(".validation-input").each(function(i, inputElement) {
      if (!isValid($(inputElement))) {
        isAllValid = false;
        var help = $(inputElement).parent().next();
        if (!help.hasClass('validation-help')) {
          help = help.next().next();
        }

        if (help.children().css('display') === 'none') {
          help.children().show(0).delay(1500).hide(0);
        }
      }
    });

    if (!isAllValid) {
      event.preventDefault()
    }
  });

  // Validate.
  function validate(inputElement) {
    if (!isWhite(inputElement) && !isDisabled(inputElement)) {
      var div = inputElement.parent();
      var span = inputElement.next();

      if (span.size() === 0) {
        inputElement.after("<span></span>");
        span = inputElement.next();
      }

      isValid(inputElement) ? success(div, span) : error(div, span);
    }
  }

  // Check the given input is valid.
  function isValid(inputElement) {
    var input = inputElement.val().trim();
    if (input === '') {
      return false;
    } else {
      var format = inputElement.attr('format');
      var regExp = genRegExp(format);

      return regExp.test(input);
    }
  }

  // Check the given input is white.
  function isWhite(inputElement) {
    return inputElement.val().trim() === '';
  }

  // Check the given input is disabled.
  function isDisabled(inputElement) {
    return inputElement.attr('disabled') === 'disabled';
  }

  // Format and regexp map.
  function genRegExp(format) {
    var regexp;
    switch (format) {
      case 'puma-task-name':
        regexp = /^[@\w]{1,30}$/;
        break;

      case 'server-name':
      case 'db-name':
        regexp = /^\w{1,30}$/;
        break;

      case 'ip':
        regexp = /^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(:(\d{1,5}))*$/;
        break;

      case 'db-server-id':
        regexp = /^\d{1,30}$/;
        break;

      case 'username':
        regexp = /^\w{1,30}$/;
        break;

      case 'password':
        regexp = /^.{1,30}$/;
        break;

      case 'bin-log-file':
        regexp = /^[\w\-. ]+$/;
        break;

      case 'bin-log-position':
        regexp = /^\d{1,30}$/;
        break;

      case 'day':
        regexp = /^\d{1,2}$/;
        break;

      default:
        regexp = undefined;
    }
    return regexp;
  }

  // Clear all the validation hint.
  function clear(div, span) {
    div.removeClass('has-success');
    div.removeClass('has-warning');
    div.removeClass('has-error');

    span.removeClass('glyphicon-ok');
    span.removeClass('glyphicon-warning-sign');
    span.removeClass('glyphicon-remove');
  }

  // Set validation success hint.
  function success(div, span) {
    clear(div, span);
    div.addClass('has-feedback has-success');
    span.addClass('glyphicon glyphicon-ok form-control-feedback');
  }

  // Set validation warning hint.
  function warning(div, span) {
    clear(div, span);
    div.addClass('has-feedback has-warning');
    span.addClass('glyphicon glyphicon-warning-sign form-control-feedback');
  }

  // Set validation error hint.
  function error(div, span) {
    clear(div, span);
    div.addClass('has-feedback has-error');
    span.addClass('glyphicon glyphicon-remove form-control-feedback');
  }
});