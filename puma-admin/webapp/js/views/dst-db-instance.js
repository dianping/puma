/**
 * jQuery event binding.
 */
$(function() {
  $("#dst-db-instance-create-form").submit(function(event) {
    event.preventDefault();
    util.submit($(this));
  });
});
