/**
 * jQuery event binding.
 */
$(function() {
  $("#dst-db-instance-create-form").submit(function() {
    util.submit($(this));
  });
});
