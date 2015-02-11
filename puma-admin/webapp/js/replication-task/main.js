/**
 * Init the replication task table.
 */
(function init() {
  var status = {
    status: 'CONNECTING'
  };
  var trs = $("tr[id|='tr']");
  trs.each(function(i, tr) {
    setReplicationTaskStatus(tr.id.replace('tr-', ''), status);
  });
}());

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
  $("#tr-" + taskId).addClass(colorClass);
  $("#status-" + taskId).text(textStatus);
  $("#binlogInfo-" + taskId).text(textBinlogInfo);
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

