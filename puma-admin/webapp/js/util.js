function formSubmit(form) {
  $.ajax({
    type    : $(form).attr('method'),
    url     : $(form).attr('action'),
    data    : $(form).serialize(),
    dataType: "json",
    success : $(form).attr('onSuccess'),
    error   : pumadmin.httpError
  });
  return false;
}

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