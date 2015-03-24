$(function(w) {

  // Warn modal.
  $("#warn-modal").on('show.bs.modal', function(event) {
    var button  = $(event.relatedTarget);
    var subpath = button.data('subpath');
    var path    = button.data('path');
    var name    = button.data('name');

    var modal = $(this);

    modal.find('.modal-title').text('警告');
    modal.find('.modal-text').text(warnText(path));
    modal.find('#modal-form-name').val(name);
    modal.find('.puma-form')
      .attr('action', window.contextpath + '/' + path + '/' + subpath)
      .attr('target', window.contextpath + '/' + path);
  });


  // Generate warn modal text.
  function warnText(category) {
    switch(category) {
      case 'src-db-instance':
      case 'dst-db-instance':
      case 'puma-server':
      case 'sync-server':
        return '确认删除该配置？';
      case 'puma-task':
      case 'sync-task':
        return '确认删除该任务？';
      default :
        alert('内部错误');
    }
  }

  // Error interface.
  w.pumaModal = {
    error: {
      // Mongo.
      storage: function() {
        errorModal('MongoDB出错，请重新进行操作');
      },

      // Swallow.
      notify: function() {
        errorModal('Swallow出错，请重新进行操作');
      },

      // HTTP.
      network: function() {
        errorModal('网络出错，请重新进行操作');
      },

      // Lock.
      lock: function() {
        errorModal('配置已锁定，删除相关任务后，请重新进行操作');
      },

      // Duplicated setting.
      duplicated: function() {
        errorModal('设置出错：设置已存在。请重新进行设置');
      },

      // Illegal setting.
      illegal: function() {
        errorModal('设置出错：非法设置。请重新进行设置');
      },

      // Miscellaneous.
      misc: function() {
        errorModal('服务器错误。请重新进行设置');
      }
    }
  };

  // Generate error modal.
  function errorModal(text) {
    var modal = $("#error-modal");
    modal.find('.modal-title').text('出错了');
    modal.find('.modal-text').text(text);
    modal.modal('show');
  }
});
