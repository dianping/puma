$(function() {
  $("#puma-server-create-form").submit(function(event) {
    event.preventDefault();
    util.submit($(this));
  });
});
