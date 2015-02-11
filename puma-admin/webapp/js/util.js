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