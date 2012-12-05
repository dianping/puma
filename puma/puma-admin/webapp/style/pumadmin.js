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
		"create_step1_next" : function() {
			//判断上一步是否有保存
			if (!w.syncConfigObjectId) {
				pumadmin.appError("错误信息", "先保存，才能到下一步");
				return;
			}
			pumadmin.next();
			//根据w.syncConfigObjectId加载DumpConfig TODO
			
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
				success : pumadmin.loadSyncConfigsDone(param.pageNum),
				error : pumadmin.httpError
			});
		},
		"loadSyncConfigsDone" : function(pageNum) {
			return function(data) {
				if (data.success == false) {
					pumadmin.appError("错误信息", data.errorMsg);
				} else {
					// 设置offer总页数
					w.totalPage = data.totalPage;
					// 如果没有offer，则显示没有offer的警告
					if (data.syncConfigs.length <= 0) {
						$("#noResultDiv").show();
						$('#resultTable').html("");
						return;
					}
					// 检查上一页和下一页是否可以点击
					w.pageNum = pageNum;
					if (pageNum >= w.totalPage) {
						$("#nextPage").addClass("disabled");
						$("#nextPage > a").attr("href", "javascript:;");
					} else {
						$("#nextPage").removeClass("disabled");
						$("#nextPage > a")
								.attr("href",
										"javascript:pumadmin.loadSyncConfigs(window.pageNum+1)");
					}
					if (pageNum <= 1) {
						$("#prePage").addClass("disabled");
						$("#prePage > a").attr("href", "javascript:;");
					} else {
						$("#prePage").removeClass("disabled");
						$("#prePage > a")
								.attr("href",
										"javascript:pumadmin.loadSyncConfigs(window.pageNum-1)");
					}
					var syncConfigs = data.syncConfigs;
					$("#resultTable").html("");
					if (data.syncConfigs.length <= 0) {
						$("#noResultDiv").show();
					} else {
						$.each(syncConfigs, function(i, el) {
							pumadmin.appendSyncConfig(el);
						});
						$("#noResultDiv").hide();
					}
				}
			};
		},
		"loadSyncConfigsForWatch" : function(pageNum) {
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
				success : pumadmin.loadSyncConfigsForWatchDone(param.pageNum),
				error : pumadmin.httpError
			});
		},
		"loadSyncConfigsForWatchDone" : function(pageNum) {
			return function(data) {
				if (data.success == false) {
					pumadmin.appError("错误信息", data.errorMsg);
				} else {
					// 设置offer总页数
					w.totalPage = data.totalPage;
					// 如果没有offer，则显示没有offer的警告
					if (data.syncConfigs.length <= 0) {
						$("#noResultDiv").show();
						$('#resultTable').html("");
						return;
					}
					// 检查上一页和下一页是否可以点击
					w.pageNum = pageNum;
					if (pageNum >= w.totalPage) {
						$("#nextPage").addClass("disabled");
						$("#nextPage > a").attr("href", "javascript:;");
					} else {
						$("#nextPage").removeClass("disabled");
						$("#nextPage > a")
								.attr("href",
										"javascript:pumadmin.loadSyncConfigs(window.pageNum+1)");
					}
					if (pageNum <= 1) {
						$("#prePage").addClass("disabled");
						$("#prePage > a").attr("href", "javascript:;");
					} else {
						$("#prePage").removeClass("disabled");
						$("#prePage > a")
								.attr("href",
										"javascript:pumadmin.loadSyncConfigs(window.pageNum-1)");
					}
					var syncConfigs = data.syncConfigs;
					$("#resultTable").html("");
					if (data.syncConfigs.length <= 0) {
						$("#noResultDiv").show();
					} else {
						$.each(syncConfigs, function(i, el) {
							pumadmin.appendSyncConfigForWatch(el);
						});
						$("#noResultDiv").hide();
					}
				}
			};
		},
		"objectId2String" : function(objectId) {
			return objectId._inc + "_" + objectId._machine + "_"
					+ objectId._time;
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
		"appendSyncConfigForWatch" : function(syncConfig) {
			// 连接
			var id = syncConfig.id._inc + "_" + syncConfig.id._machine + "_"
					+ syncConfig.id._time;
			var link = "<a href=\"javascript:pumadmin.watchSyncConfig('" + id
					+ "')\">具体状态 »</a>";
			// 拼装
			var html = "<tr><td>" + syncConfig.src.pumaServerHost + "</td><td>"
					+ syncConfig.src.serverId + "</td><td>"
					+ syncConfig.src.target + "</td><td>"
					+ syncConfig.dest.host + "</td><td>" + link + "</td><tr>";
			$("#resultTable").append(html);
		},
		//显示配置信息，实时：显示binlog进度，操作：暂停，启动，追赶
		"watchSyncConfig" : function(mergeId) {
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
				w.syncConfigObjectId = data.id;
				pumadmin.appError("信息", "保存成功");
			}
		},
		"modifySyncXml" : function() {
			var param = new Object();
			param.syncXmlString = w.xmlEditor.getValue();
			param.mergeId = pumadmin.objectId2String(w.syncXmlObjectId);
			var url = w.contextpath + '/modifySyncXml';
			$.ajax({
				type : 'POST',
				url : url,
				data : param,
				dataType : "json",
				success : pumadmin.modifySyncXmlDone,
				error : pumadmin.httpError
			});
		},
		"modifySyncXmlDone" : function(data) {
			if (data.success == false) {
				pumadmin.appError("错误信息", data.errorMsg);
			} else {
				pumadmin.appError("信息", "修改成功");
			}
		},
		"step1_next" : function() {
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
