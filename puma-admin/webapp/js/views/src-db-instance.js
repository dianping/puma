/**
 * jQuery event binding.
 */
$(function() {
  $("#src-db-instance-create-form").submit(function() {
    util.submit($(this));
  });
});
