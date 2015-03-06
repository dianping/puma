$(function(w) {

  function submit(jForm) {
    $.ajax({
      url     : $(jForm).attr('action'),
      type    : $(jForm).attr('method'),
      data    : $(jForm).serialize(),
      dataType: 'json',
      success : function (res) {
        // Hide the warn modal first.
        $("#warn-modal").modal('hide');

        if (res.success) {
          window.location = $(jForm).attr('target')
        } else {
          w.pumaModal.error[res.error]();
        }
      },
      error  : function() {
        // Hide the warn modal first.
        $("#warn-modal").modal('hide');

        w.pumaModal.error['network']();
      }
    });
  }

  $(".puma-form").on('submit', function(event) {
    event.preventDefault();
    submit($(this));
  });

});