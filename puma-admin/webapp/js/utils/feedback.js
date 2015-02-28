var feedback = {

  clear: function(form, icon) {
    form.removeClass('has-success');
    form.removeClass('has-warning');
    form.removeClass('has-error');

    icon.removeClass('glyphicon-ok');
    icon.removeClass('glyphicon-warning-sign');
    icon.removeClass('glyphicon-remove');
  },

  success: function(form, icon) {
    feedback.clear(form, icon);
    form.addClass('has-success');
    icon.addClass('glyphicon-ok');
  },

  warning: function(form, icon) {
    feedback.clear(form, icon);
    form.addClass('has-warning');
    icon.addClass('glyphicon-warning-sign');
  },

  error: function(form, icon) {
    feedback.clear(form, icon);
    form.addClass('has-error');
    icon.addClass('glyphicon-remove');
  }
};