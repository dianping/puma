$(function() {

  // Operations.

  // Refresh puma task state.
  function refreshPumaTaskState(id, cb) {
    $.ajax({
      url     : window.contextpath + '/puma-task/refresh',
      type    : 'POST',
      data    : {id: id},
      dataType: 'json',
      success : function(res) {
        if (!res.success) {
          return cb(res.err);
        }

        cb(null, res.state);
      },
      error  : function() {
        cb('网络出现问题');
      }
    });
  }

  // Set puma task status.
  function setPumaTaskStatus(td, status) {
    var statusStr;

    switch (status) {
      case 'WAITING':
        statusStr = '等待中';
        break;
      case 'PREPARING':
        statusStr = '准备中';
        break;
      case 'RUNNING':
        statusStr = '运行中';
        break;
      case 'STOPPING':
        statusStr = '停止中';
        break;
      case 'STOPPED':
        statusStr = '已停止';
        break;
      case 'FAILED':
        statusStr = '已失败';
        break;
      default:
        statusStr = '内部错误';
    }

    td.text(statusStr);
  }

  // Set puma task bin log info.
  function setPumaTaskBinlogInfo(td, binlogInfo) {
    var binlogFileStr = ((binlogInfo && binlogInfo.binlogFile) ? binlogInfo.binlogFile : '--');
    var binlogFilePositionStr = ((binlogInfo && binlogInfo.binlogPosition) ? binlogInfo.binlogPosition : '--');

    td.text(binlogFileStr + ' | ' + binlogFilePositionStr);
  }

  // puma-task
  $(".puma-task-refresh").on('click', function(event) {
    event.preventDefault();

    var taskId       = $(this).attr('data-id');
    var parent       = $(this).parent();
    var tdBinlogInfo = parent.prev();
    var tdStatus     = tdBinlogInfo.prev();

    refreshPumaTaskState(taskId, function(err, state) {
      if (err) {
        alert(err);
      } else {
        setPumaTaskStatus(tdStatus, state.status);
        setPumaTaskBinlogInfo(tdBinlogInfo, state.binlogInfo);
      }
    });
  }).trigger('click');
});


