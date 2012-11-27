(function(w) {
	var pumadmin = {
		"pre" : function() {
			var index = $("#stepsBreak > li[class='step_active']").index();
			index = index - 1;
			$("#stepsBreak > li[class='step_active']").attr("class",
					"step_undo");
			$("#stepsBreak > li:eq(" + index + ")")
					.attr("class", "step_active");
			$("#stepsCarousel").carousel('prev');
		},
		"next" : function() {
			var stepsBreak = $("#stepsBreak > li");
			var length = stepsBreak.size();
			var index = $("#stepsBreak > li[class='step_active']").index();
			if (index + 1 > length - 1) {
				stepsBreak.attr("class", "step_undo");
			} else {
				$("#stepsBreak > li[class='step_active']").attr("class",
						"step_done");
			}
			index = (index + 1) % length;
			$("#stepsBreak > li:eq(" + index + ")")
					.attr("class", "step_active");
			$("#stepsCarousel").carousel('next');
		},
		"loadSyncConfigs" : function(pageNum) {
			var param = new Object();
			if (typeof pageNum != 'undefined' && pageNum > 0) {
				param.pageNum = pageNum;
			} else {
				param.pageNum = 1;
			}
			var url = w.contextpath + '/loadSyncConfigs';
			$.ajax({
				type : 'POST',
				url : url,
				data : param,
				dataType : "json",
				success : pumadmin.loadSyncConfigDone,
				error : pumadmin.httpError
			});
		},
		"loadSyncConfigDone" : function(data) {
			if (data.success == false) {
				pumadmin.appError("错误信息", data.errorMsg);
			} else {
				console.log(data);
				var syncConfigs = data.syncConfigs;
				if (data.syncConfigs.length <= 0) {
					$("#noResultDiv").show();
				} else {
					$.each(syncConfigs, function(i, el) {
						pumadmin.appendSyncConfig(el);
					});
					$("#noResultDiv").hide();
				}
			}
		},
		"appendSyncConfig" : function(syncConfig) {
			// 连接
			var id = syncConfig.id._inc + "_" + syncConfig.id._machine + "_"
					+ syncConfig.id._time;
			var link = "<a href=\"javascript:pumadmin.loadSyncXml('" + id
					+ "')\">编辑 »</a>";
			// 拼装
			var html = "<tr><td>" + syncConfig.src.pumaServerHost + "</td><td>"
					+ syncConfig.src.serverId + "</td><td>"
					+ syncConfig.src.target + "</td><td>"
					+ syncConfig.dest.host + "</td><td>" + link + "</td><tr>";
			$("#resultTable").append(html);
		},
		"loadSyncXml" : function(mergeId) {
			pumadmin.next();
			var param = new Object();
			param.mergeId = mergeId;
			var url = w.contextpath + '/loadSyncXml';
			$.ajax({
				type : 'POST',
				url : url,
				data : param,
				dataType : "json",
				success : pumadmin.loadSyncXmlDone,
				error : pumadmin.httpError
			});
		},
		"loadSyncXmlDone" : function(data) {
			if (data.success == false) {
				pumadmin.appError("错误信息", data.errorMsg);
			} else {
				w.xmlEditor.setValue(data.syncXml.xml);
				w.syncXmlObjectId = data.syncXml.id;
				w.xmlEditor.moveCursorTo(0, 0);
			}
		},
		"saveSyncXml" : function() {
			var param = new Object();
			param.syncXmlString = w.xmlEditor.getValue();
			var url = w.contextpath + '/saveSyncXml';
			$.ajax({
				type : 'POST',
				url : url,
				data : param,
				dataType : "json",
				success : pumadmin.saveSyncXmlDone,
				error : pumadmin.httpError
			});
		},
		"saveSyncXmlDone" : function(data) {
			if (data.success == false) {
				pumadmin.appError("错误信息", data.errorMsg);
			} else {
				pumadmin.appError("信息", "保存成功");
			}
		},
		"step1_next" : function() {
		},
		"modifySyncXml" : function() {
		},
		"appError" : function(title, errorMsg) {
			pumadmin.alertError(title, errorMsg);
		},
		"httpError" : function(xhr, textStatus, errorThrown) {
			pumadmin.alertError('抱歉啦', '抱歉，网络发生错误了，请刷新页面试试...');
		},
		"alertError" : function(title, errorMsg) {
			// 显示错误消息
			$('#errorMsg > div[class="modal-header"] > h3').text(title);
			$('#errorMsg > div[class="modal-body"] > p').text(errorMsg);
			$('#errorMsg').modal('show');
		}
	};
	w.pumadmin = pumadmin;
}(window || this));
