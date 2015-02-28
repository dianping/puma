$(function() {
  $("#warn-modal").on('show.bs.modal', function(event) {
    var button  = $(event.relatedTarget);
    var subpath = button.data('subpath');
    var path    = button.data('path');
    var id      = button.data('id');

    var modal = $(this);
    modal.find('.modal-title').text(getModalTitle(path, subpath));
    modal.find('.modal-body h4').text(getModalBody(path, subpath));
    modal.find('.modal-footer form input').val(getModalContent(id));

    var location = modal.find('.modal-footer form').attr('location');
    modal.find('.modal-footer form')
      .attr('action', location + '/' + path + '/' + subpath)
      .attr('target', location + '/' + path);
  });

  $('#warn-modal-form').submit(function(event) {
    event.preventDefault();
    util.submit($(this));
  });
});

function getModalTitle(path, subpath) {
  switch (subpath) {
    case 'remove':
      return '删除配置';
  }
}

function getModalBody(path, subpath) {
  switch (subpath) {
    case 'remove':
      return '确认删除该配置?';
  }
}

function getModalContent(option) {
  return option;
}