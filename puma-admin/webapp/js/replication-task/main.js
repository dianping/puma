/**
 * Set replication task status.
 *
 * @param taskId
 * @param status
 */
function setReplicationTaskStatus(taskId, status) {
  var status     = status.status
    , binlogInfo = status.binlogInfo
    ;

  var textStatus     = ''
    , textBinlogInfo = '已执行：'
    , colorClass     = ''
    ;

  switch(status) {
    case 'CONNECTING':
      textStatus     = '连接中';
      textBinlogInfo += '--';
      colorClass     = 'warning';
      break;
    case 'WAITING':
      textStatus     = '等待中';
      textBinlogInfo += '--';
      colorClass     = 'warning';
      break;
    case 'RUNNING':
      textStatus     = '运行中';
      textBinlogInfo += binlogInfo.binlogFile + ' | ' + binlogInfo.binlogPosition;
      colorClass     = '';
      break;
    case 'FAILURE':
      textStatus     = '失败';
      textBinlogInfo += binlogInfo.binlogFile + ' | ' + binlogInfo.binlogPosition;
      colorClass     = 'danger';
      break;
  }

  removeTableColors($("#replicationTaskTable"));
  $("#" + escape("tr-" + taskId)).addClass(colorClass);
  $("#" + escape("status-" + taskId)).text(textStatus);
  $('#' + escape("binlogInfo-" + taskId)).text(textBinlogInfo);
}

/**
 * Update replication task status.
 *
 * @param taskId
 */
function updateReplicationTask(taskId) {
  var url = window.contextpath + '/replicationTask/refresh';
  $.ajax(url, {
    type    : 'POST',
    data    : {taskId: taskId},
    dataType: 'json',
    success : function(response) {
      if (!response.success) {
        pumadmin.appError('错误信息', response.err);
      } else {
        setReplicationTaskStatus(taskId, response.data);
      }
    },
    error   : pumadmin.httpError
  });
}

function removeReplicationTask() {
  var url = window.contextpath + 'replicationTask/remove';
  $.ajax(url, {
    type    : 'POST',
    data    : {taskId: $("#removeReplicationTaskInput").val()},
    dataType: 'json',
    success : function(response) {
      if (!response.success) {
        pumadmin.appError('错误信息', response.err);
      } else {
        window.location = window.contextpath + '/replicationTask';
      }
    },
    error   : pumadmin.httpError
  });
}

function removeReplicationTaskModal(taskId) {
  $("#removeReplicationTaskInput").val(taskId);
  $("#removeReplicationTaskModal").modal('show');
}

var rTask = {

  init: function() {
    var trs = $("tr[id|='tr']");
    trs.each(function(i, tr) {
      rTask.updateRTask(tr.id.replace('tr-', ''));
    });
  },

  setRTask: function(id, data) {
    var status     = data.status
      , binlogInfo = data.binlogInfo
      ;

    var textStatus     = ''
      , textBinlogInfo = '已执行：'
      , colorClass     = ''
      ;

    switch(status) {
      case 'CONNECTING':
        textStatus     = '连接中';
        textBinlogInfo += '--';
        colorClass     = 'warning';
        break;
      case 'WAITING':
        textStatus     = '等待中';
        textBinlogInfo += '--';
        colorClass     = 'warning';
        break;
      case 'RUNNING':
        textStatus     = '运行中';
        textBinlogInfo += binlogInfo.binlogFile + ' | ' + binlogInfo.binlogPosition;
        colorClass     = '';
        break;
      case 'FAILURE':
        textStatus     = '失败';
        textBinlogInfo += binlogInfo.binlogFile + ' | ' + binlogInfo.binlogPosition;
        colorClass     = 'danger';
        break;
    }

    removeTableColors($("#replicationTaskTable"));
    $("#" + escape("tr-" + id)).addClass(colorClass);
    $("#" + escape("status-" + id)).text(textStatus);
    $('#' + escape("binlogInfo-" + id)).text(textBinlogInfo);
  },

  updateRTask: function(id) {
    var url = window.contextpath + 'replicationTask/update';
    $.ajax(url, {
      type    : 'POST',
      data    : {id: id},
      dataType: 'json',
      success : function(response) {
        rTask.updateRTaskOnSuccess(id, response);
      }
    });
  },

  updateRTaskOnSuccess: function(id, response) {
    if (!response.success) {
      return alert(response.error);
    }

    rTask.setRTask(id, response.data);
  }
};

rTask.init();

