var util = {

  validate: {
    clear: function(div, span) {
      div.removeClass('has-success');
      div.removeClass('has-warning');
      div.removeClass('has-error');

      span.removeClass('glyphicon-ok');
      span.removeClass('glyphicon-warning-sign');
      span.removeClass('glyphicon-remove');
    },

    success: function(div, span) {
      util.validate.clear(div, span);
      div.addClass('has-feedback has-success');
      span.addClass('glyphicon glyphicon-ok form-control-feedback');
    },

    warning: function(div, span) {
      util.validate.clear(div, span);
      div.addClass('has-feedback has-warning');
      span.addClass('glyphicon glyphicon-warning-sign form-control-feedback');
    },

    error: function(div, span) {
      util.validate.clear(div, span);
      div.addClass('has-feedback has-error');
      span.addClass('glyphicon glyphicon-remove form-control-feedback');
    }
  },

  /**
   *
   *
   * @param form
   */
  submit: function (form) {
    $.ajax({
      url     : $(form).attr('action'),
      type    : $(form).attr('method'),
      data    : $(form).serialize(),
      dataType: 'json',
      success : function (res) {
        if (res.success) {
          window.location = $(form).attr('target')
        } else {
          alert("failure");
        }
      },
      error  : function(err) {
        alert(err);
      }
    });
  }
};

function removeTableColors(table) {
  table.removeClass("active");
  table.removeClass("success");
  table.removeClass("warning");
  table.removeClass("danger");
  table.removeClass("info");
}

function escape(id) {
  return id.replace(/([;&,\.\+\*~':"!\^#$%@\[\]\(\)=>\|])/g, '\\$1');
}