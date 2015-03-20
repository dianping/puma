$(function(w) {

  var STATE_MAP = {
    WAITING     : "等待中",
    PREPARING   : "准备中",
    RUNNING     : "运行中",
    SUSPENDED   : "已暂停",
    STOPPING    : "已停止",
    SUCCESS     : "结束-成功",
    RECONNECTING: "重新连接",
    DUMPING     : "备份中",
    LOADING     : "加载中",
    FAILED      : "结束-失败",
    DISCONNECTED: "失去连接"
  };

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

  // Refresh sync task state.
  function refreshSyncTaskState(name, cb) {
    $.ajax({
      url     : window.contextpath + '/sync-task/refresh',
      type    : 'POST',
      data    : {name: name},
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

  // Set sync task status.
  function setSyncTaskStatus(td, status) {
    td.text(STATE_MAP[status]);
  }

  // Set sync task bin log info.
  function setSyncTaskBinlogInfo(td, binlogInfo) {
    var binlogFileStr = ((binlogInfo && binlogInfo.binlogFile) ? binlogInfo.binlogFile : '--');
    var binlogFilePositionStr = ((binlogInfo && binlogInfo.binlogPosition) ? binlogInfo.binlogPosition : '--');

    td.text(binlogFileStr + ' | ' + binlogFilePositionStr);
  }

  // sync-task
  $(".sync-task-refresh").on('click', function(event) {
    event.preventDefault();

    var taskName     = $(this).attr('data-name');
    var parent       = $(this).parent();
    var tdBinlogInfo = parent.prev();
    var tdStatus     = tdBinlogInfo.prev();

    refreshSyncTaskState(taskName, function(err, state) {
      if (err) {
        alert(err);
      } else {
        setSyncTaskStatus(tdStatus, state.status);
        setSyncTaskBinlogInfo(tdBinlogInfo, state.binlogInfoOfIOThread);
      }
    });
  }).trigger('click');

  $("#srcDBInstanceName").on('change', function() {
    $("#name").val(genPumaTaskName());
  }).trigger('change');

  $("#pumaServerName").on('change', function() {
    $("#name").val(genPumaTaskName());
  }).trigger('change');

  function genPumaTaskName() {
    return $("#srcDBInstanceName").val() + '@' + $("#pumaServerName").val();
  }

  // Puma task resume.
  $(".puma-task-resume").on('click', function(event) {
    event.preventDefault();
    var id = $(this).attr('data-id');

    $.ajax({
      url     : window.contextpath + '/puma-task' + '/resume',
      type    : 'POST',
      data    : {id: id},
      dataType: 'json',
      success : function(res) {
        if (res.success) {
          window.location = window.contextpath + '/puma-task';
        } else {
          w.pumaModal.error[res.error]();
        }
      },
      error   : function() {
        w.pumaModal.error['network']();
      }
    });
  });

  // Puma task pause.
  $(".puma-task-pause").on('click', function(event) {
    event.preventDefault();
    var id = $(this).attr('data-id');

    $.ajax({
      url     : window.contextpath + '/puma-task' + '/pause',
      type    : 'POST',
      data    : {id: id},
      dataType: 'json',
      success : function(res) {
        if (res.success) {
          window.location = window.contextpath + '/puma-task';
        } else {
          w.pumaModal.error[res.error]();
        }
      },
      error   : function() {
        w.pumaModal.error['network']();
      }
    });
  });
});


