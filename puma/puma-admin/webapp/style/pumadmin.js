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
		"create_step2_next" : function() {
			pumadmin.next();
			// 加载pumaserverId列表
			pumadmin.loadSyncServerList();
		},
		"saveBinlog" : function() {
			var param = new Object();
			param.binlogFile = $("#binlogFileInput").val();
			param.binlogPosition = $("#binlogPositionInput").val();
			var url = w.contextpath + '/saveBinlog';
			$.ajax({
				type : 'POST',
				url : url,
				data : param,
				dataType : "json",
				success : pumadmin.saveBinlogDone,
				error : pumadmin.httpError
			});
			$("#saveBinlogSuccess").hide();
			$("#saveBinlogError").hide();
		},
		"saveBinlogDone" : function(data) {
			if (data.success == false) {
				$("#saveBinlogErrorCause").text(data.errorMsg);
				$("#saveBinlogError").show();
			} else {
				$("#saveBinlogSuccess").show();
			}
		},
		"loadSyncServerList" : function() {
			var param = new Object();
			var url = w.contextpath + '/loadSyncServerList';
			$.ajax({
				type : 'POST',
				url : url,
				data : param,
				dataType : "json",
				success : pumadmin.loadSyncServerListDone,
				error : pumadmin.httpError
			});
		},
		"loadSyncServerListDone" : function(data) {
			if (data.success == false) {
				pumadmin.appError("错误信息", data.errorMsg);
			} else {
				$('#pumaSyncServerId').html('');
				$.each(data.syncServerHosts, function(i, el) {
					$('#pumaSyncServerId').append(new Option(el, el));
				});
			}
		},
		"loadDumpConfig" : function() {
			var param = new Object();
			// var mergeId = pumadmin.objectId2MergeId(w.syncConfigObjectId);
			// param.mergeId = mergeId;
			var url = w.contextpath + '/loadDumpConfig';
			$.ajax({
				type : 'POST',
				url : url,
				data : param,
				dataType : "json",
				success : pumadmin.loadDumpConfigDone,
				error : pumadmin.httpError
			});
		},
		"loadDumpConfigDone" : function(data) {
			if (data.success == false) {
				pumadmin.appError("错误信息", data.errorMsg);
			} else {
				var dumpConfig = data.dumpConfig;
				var dumpSrcText = dumpConfig.src.host + " (username="
						+ dumpConfig.src.username + ")";
				$('#dumpSrc').val(dumpSrcText);
				var dumpDestText = dumpConfig.dest.host + " (username="
						+ dumpConfig.dest.username + ")";
				$('#dumpDest').val(dumpDestText);
				// 展示数据同步的映射关系
				$("#tables").html('');
				$.each(dumpConfig.databaseConfigs, function(i, el) {
					var html = "";
					html += "<table class=\"table table-hover\">";
					html += "<thead><tr>";
					html += "<th>" + el.from + "</th>";
					html += "<th>" + el.to + "</th>";
					html += "</tr>";
					html += "</thead>";
					html += "<tbody>";
					$.each(el.tables, function(i, el) {
						html += "<tr>";
						html += "<td>" + el.from + "</td>";
						html += "<td>" + el.from + "</td>";
						html += "</tr>";
					});
					html += "</tbody>";
					html += "</table>";
					$("#tables").append(html);
				});

			}
		},
		"dump" : function() {
			var param = new Object();
			var url = w.contextpath + '/dump';
			$.ajax({
				type : 'POST',
				url : url,
				data : param,
				dataType : "json",
				success : pumadmin.dumpDone,
				error : pumadmin.httpError
			});
			// 按钮变换
			$("#dumpButton").attr("disabled", "disabled");
			$("#success").hide();
			$("#fail").hide();
			$("#binlogFile").text("");
			$("#binlogPosition").text("");
		},
		"dumpDone" : function(data) {
			if (data.success == false) {
				// 显示错误消息
				pumadmin.appError("尝试运行时发生错误", data.errorMsg);
				// 去掉按钮disable
				$('#dumpButton').removeAttr('disabled');
			} else {
				// 开始显示控制台
				$('#console').text('');
				pumadmin.dumpConsole();
			}
		},
		"dumpConsole" : function(data) {
			var param = new Object();
			// 发送ajax请求jsonp
			var url = w.contextpath + '/dumpConsole';
			$.ajax({
				type : 'POST',
				url : url,
				data : param,
				dataType : "json",
				success : pumadmin.dumpConsoleDone,
				error : pumadmin.httpError
			});
		},
		"dumpConsoleDone" : function(data) {
			if (data.success == false) {
				pumadmin.appError("访问控制台时发生错误", data.errorMsg);
				// 去掉按钮disable
				$('#dumpButton').removeAttr('disabled');
			} else {
				if (data.status == 'continue') {// 继续运行
					// 显示到控制台
					w.dumpLastLine = data.content;
					$('#console').text($('#console').text() + data.content);// append()和html()一样不做转义，所以使用text()
					$("#console").scrollTop($("#console")[0].scrollHeight);
					pumadmin.dumpConsole();
				} else {// 运行已经停止
					// 获取结果
					var binlogSign = "dump&load done. binloginfo:";
					if (pumadmin.startWith(w.dumpLastLine, binlogSign)) {
						var binlogJson = w.dumpLastLine
								.substring(binlogSign.length);
						console.log(binlogJson);
						var binlogInfo = $.parseJSON(binlogJson);
						$("#binlogFile").text(binlogInfo.binlogFile);
						$("#binlogPosition").text(binlogInfo.binlogPosition);
						$("#success").show();
						// 获取binlog位置，如果没有则为空
						$("#binlogFileInput").val($("#binlogFile").text());
						$("#binlogPositionInput").val(
								$("#binlogPosition").text());
					} else {
						// 显示dump失败
						$("#fail").show();
						// 去掉按钮disable
						$('#dumpButton').removeAttr('disabled');
					}
				}
			}
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
			var id = pumadmin.objectId2MergeId(syncConfig.id);
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
			var mergeId = pumadmin.objectId2MergeId(syncConfig.id);
			var link = "<a href=\"javascript:pumadmin.watchSyncConfig('"
					+ mergeId + "')\">具体状态 »</a>";
			// 拼装
			var html = "<tr><td>" + syncConfig.src.pumaServerHost + "</td><td>"
					+ syncConfig.src.serverId + "</td><td>"
					+ syncConfig.src.target + "</td><td>"
					+ syncConfig.dest.host + "</td><td>" + link + "</td><tr>";
			$("#resultTable").append(html);
		},
		// 显示配置信息，实时：显示binlog进度，操作：暂停，启动，追赶
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
				// w.syncConfigObjectId = data.id;
				pumadmin.next();
				// 加载DumpConfig
				pumadmin.loadDumpConfig();
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
		"objectId2MergeId" : function(objectId) {
			return objectId._inc + "_" + objectId._machine + "_"
					+ objectId._time;
		},
		"appError" : function(title, errorMsg) {
			pumadmin.alertError(title, errorMsg);
		},
		"httpError" : function(xhr, textStatus, errorThrown) {
			// 去掉按钮disable
			$('#dumpButton').removeAttr('disabled');
			pumadmin.alertError('抱歉啦', '抱歉，网络发生错误了，请刷新页面试试...');
		},
		"alertError" : function(title, errorMsg) {
			// 显示错误消息
			$('#errorMsg > div[class="modal-header"] > h3').text(title);
			$('#errorMsg > div[class="modal-body"] > p').text(errorMsg);
			$('#errorMsg').modal('show');
		},
		"endWith" : function(s, endStr) {
			if (s == null || s == "" || s.length == 0
					|| endStr.length > s.length)
				return false;
			if (s.substring(s.length - endStr.length) == endStr)
				return true;
			else
				return false;
			return true;
		},
		"startWith" : function(s, preStr) {
			if (s == null || s == "" || s.length == 0
					|| preStr.length > s.length)
				return false;
			if (s.substr(0, preStr.length) == preStr)
				return true;
			else
				return false;
			return true;
		}
	};
	w.pumadmin = pumadmin;
}(window || this));
