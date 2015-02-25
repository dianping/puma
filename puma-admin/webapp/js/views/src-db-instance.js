/**
 * jQuery event binding.
 */
$(function() {
  $("#src-db-instance-create-form").submit(function(event) {
    event.preventDefault();
    util.submit($(this));
  });
});
