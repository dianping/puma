$(function() {

  function genModal(modal, title, text) {
    modal.find('.modal-title').text(title);
    modal.find('.modal-text').text(text);
    modal.show();
  }

  function genErrorModal(text) {
    genModal($("error-modal"), '出错了', text);
  }

  var error = {

    // Mongo.
    storage: function() {
      genErrorModal('MongoDB出错，请重新进行操作');
    },

    // Swallow.
    notify: function() {
      genErrorModal('Swallow出错，请重新进行操作');
    },

    // HTTP.
    network: function() {
      genErrorModal('网络出错，请重新进行操作');
    },

    // 1. Duplicated.
    // 2. Illegal.
    setting: {
      duplicated: function() {
        genErrorModal('设置出错：设置已存在。请重新进行设置');
      },

      illegal: function() {
        genErrorModal('设置出错：非法设置。请重新进行设置');
      }
    },

    // Miscellaneous.
    misc: function() {
      genErrorModal('未知错误。请重新进行设置');
    }
  };

});


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