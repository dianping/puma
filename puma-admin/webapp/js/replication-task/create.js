(function init() {
    setTaskId();
})();

$("#dbInstanceName").on("change", function() {
   setTaskId();
});

$("#serverName").on("change", function() {
    setTaskId();
});

$("#taskName").on("input", function() {
    setStorageDefaultValue($(this).val());
});

function setTaskId() {
    var dbInstanceName = $("#dbInstanceName").val();
    var taskServerName = $("#serverName").val();

    var taskId = dbInstanceName + '#' + taskServerName;
    $("#taskName").val(taskId);
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