var util = {

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
        }
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