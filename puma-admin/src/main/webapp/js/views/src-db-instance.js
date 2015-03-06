$(function() {


  $("#src-db-instance-create-form").submit(function(event) {
    event.preventDefault();
    util.submit($(this));
  });
  /*
  // Source database name dynamic checking.
  $("#name").on("change", function() {
    var name = $(this).val();

    var form = $("#name-form-group");
    var icon = $("#name-feedback-icon");

    if (validate.dbName(name)) {
      feedback.success(form, icon);
    } else {
      feedback.error(form, icon);
    }
  });*/

  // Source database host dynamic checking.
});
