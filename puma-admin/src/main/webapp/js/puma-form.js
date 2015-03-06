$(function() {

  function submit(jForm) {
    $.ajax({
      url     : $(jForm).attr('action'),
      type    : $(jForm).attr('method'),
      data    : $(jForm).serialize(),
      dataType: 'json',
      success : function (res) {
        if (res.success) {
          window.location = $(jForm).attr('target')
        } else {
          alert("failure");
        }
      },
      error  : function(err) {
        alert(err);
      }
    });
  }

  $(".puma-form-button").submit(function(event) {
    alert('hello world');
    event.preventDefault();
    submit($(this));
  });

});