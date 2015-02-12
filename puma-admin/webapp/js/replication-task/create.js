(function init() {
  setTaskId();
})();

$("#dbInstanceName").on("change", function () {
  setTaskId();
});

$("#serverName").on("change", function () {
  setTaskId();
});

$("#taskId").on("input", function () {
  setStorageDefaultValue($(this).val());
});

function setTaskId() {
  var dbInstanceName = $("#dbInstanceName").val();
  var taskServerName = $("#serverName").val();

  var taskId = dbInstanceName + '@' + taskServerName;
  $("#taskId").val(taskId);
  setStorageDefaultValue(taskId);
}

function setStorageDefaultValue(taskId) {
  var storageBaseDir = '/data/appdatas/puma/storage/';

  $("#masterStorageBaseDir").val(storageBaseDir + 'master/' + taskId);
  $("#masterBucketFilePrefix").val('Bucket-');
  $("#maxMasterBucketLengthMB").val(1000);
  $("#maxMasterFileCount").val(50);
  $("#slaveStorageBaseDir").val(storageBaseDir + 'slave/' + taskId);
  $("#slaveBucketFilePrefix").val('Bucket-');
  $("#maxSlaveBucketLengthMB").val(1000);
  $("#maxSlaveFileCount").val(50);
  $("#preservedDay").val(2);
  $("#binlogIndexBaseDir").val(storageBaseDir + 'binlogIndex/' + taskId);
}

function createFormDone(response) {
  if (!response.success) {
    pumadmin.appError('错误信息', response.err);
  } else {
    window.location = window.contextpath + '/replicationTask';
  }
}

function createSubmit(form) {
  $.ajax({
    type    : $(form).attr('method'),
    url     : $(form).attr('action'),
    data    : $(form).serialize(),
    dataType: "json",
    success : onSuccess,
    error   : pumadmin.httpError
  });
  return false;
}

function onSuccess(response) {
  if (!response.success) {
    pumadmin.appError('错误信息', response.err);
  } else {
    window.location = window.contextpath + '/replicationTask';
  }
}