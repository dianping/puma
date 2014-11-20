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
		"submit" : function(form) {
			$.ajax({
				type : $(form).attr('method'),
				url : $(form).attr('action'),
				data : $(form).serialize(),
				dataType : "json",
				success : pumadmin[$(form).attr('onSuccess')],
				error : pumadmin.httpError
			});
			return false;
		},
		"addMysqlHostInput" : function() {
			$("#mysqlHostInput").append($("#mysqlHostInputDemo").html());
		},
		"saveMysqlConfigDone" : function(data) {
			if (data.success == false) {
				pumadmin.appError("错误信息", data.errorMsg);
			} else {
				w.location = w.contextpath + '/mysqlSetting';
			}
		},
		"modifyMysqlConfigDone" : function(data) {
			if (data.success == false) {
				pumadmin.appError("错误信息", data.errorMsg);
			} else {
				pumadmin.appError("信息", "更新成功");
			}
		},
		"openDelMysqlConfigModal" : function(id) {
			$("#delMysqlConfigModal").modal('show');
			$("#delId").val(id);
		},
		"deleteMysqlConfigDone" : function(data) {
			if (data.success == false) {
				pumadmin.appError("错误信息", data.errorMsg);
			} else {
				w.location = w.contextpath + '/mysqlSetting';
			}
		},
		"addPumaServerHostInput" : function() {
			$("#pumaServerHostInput").append(
					$("#pumaServerHostInputDemo").html());
		},
		"savePumaServerConfigDone" : function(data) {
			if (data.success == false) {
				pumadmin.appError("错误信息", data.errorMsg);
			} else {
				w.location = w.contextpath + '/pumaServerSetting';
			}
		},
		"modifyPumaServerConfigDone" : function(data) {
			if (data.success == false) {
				pumadmin.appError("错误信息", data.errorMsg);
			} else {
				pumadmin.appError("信息", "更新成功");
			}
		},
		"openDelPumaServerConfigModal" : function(id) {
			$("#delPumaServerConfigModal").modal('show');
			$("#delId").val(id);
		},
		"deletePumaServerConfigDone" : function(data) {
			if (data.success == false) {
				pumadmin.appError("错误信息", data.errorMsg);
			} else {
				w.location = w.contextpath + '/pumaServerSetting';
			}
		},
		"savePumaSyncServerConfigDone" : function(data) {
			if (data.success == false) {
				pumadmin.appError("错误信息", data.errorMsg);
			} else {
				w.location = w.contextpath + '/pumaSyncServerSetting';
			}
		},
		"modifyPumaSyncServerConfigDone" : function(data) {
			if (data.success == false) {
				pumadmin.appError("错误信息", data.errorMsg);
			} else {
				pumadmin.appError("信息", "更新成功");
			}
		},
		"openDelPumaSyncServerConfigModal" : function(id) {
			$("#delPumaSyncServerConfigModal").modal('show');
			$("#delId").val(id);
		},
		"deletePumaSyncServerConfigDone" : function(data) {
			if (data.success == false) {
				pumadmin.appError("错误信息", data.errorMsg);
			} else {
				w.location = w.contextpath + '/pumaSyncServerSetting';
			}
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
				$('#syncServerHost').html('');
				$.each(data.syncServerHosts, function(i, el) {
					$('#syncServerHost').append(new Option(el, el));
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
					// 添加同步任务的已分配或为分配任务
					var syncTasks = data.syncTasks;
					if (syncTasks.length > 0) {
						$.each(syncTasks, function(i, el) {
							var mergeId = pumadmin
									.objectId2String(el.syncConfigId);
							$("#trOf" + mergeId + ">td:eq(4)").text(
									el.syncServerHost);
						});
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
					// 添加同步任务的已分配或为分配任务
					var syncTasks = data.syncTasks;
					console.log(syncTasks);
					if (syncTasks.length > 0) {
						$.each(syncTasks, function(i, el) {
							var mergeId = pumadmin
									.objectId2String(el.syncConfigId);
							console.log(mergeId);
							$("#trOf" + mergeId + ">td:eq(4)").text(
									el.syncServerHost);
						});
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
			var mergeId = pumadmin.objectId2MergeId(syncConfig.id);
			var link = "<a href=\"javascript:pumadmin.loadSyncXml('" + mergeId
					+ "')\">编辑 »</a>";
			// 拼装
			var html = "<tr id='trOf" + mergeId + "'><td>"
					+ syncConfig.src.pumaServerHost + "</td><td>"
					+ syncConfig.src.serverId + "</td><td>"
					+ syncConfig.src.target + "</td><td>"
					+ syncConfig.dest.host + "</td><td>未分配sync-server</td><td>"
					+ link + "</td><tr>";
			$("#resultTable").append(html);
		},
		"appendSyncConfigForWatch" : function(syncConfig) {
			// 连接
			var mergeId = pumadmin.objectId2MergeId(syncConfig.id);
			var link = "<a href=\"javascript:pumadmin.watchSyncConfig('"
					+ mergeId + "')\">具体状态 »</a>";
			// 拼装
			var html = "<tr id='trOf" + mergeId + "'><td>"
					+ syncConfig.src.pumaServerHost + "</td><td>"
					+ syncConfig.src.serverId + "</td><td>"
					+ syncConfig.src.target + "</td><td>"
					+ syncConfig.dest.host + "</td><td>未分配sync-server</td><td>"
					+ link + "</td><tr>";
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
		"saveSyncTask" : function() {
			var param = new Object();
			param.syncServerHost = $("#syncServerHost").val();
			var url = w.contextpath + '/saveSyncTask';
			$.ajax({
				type : 'POST',
				url : url,
				data : param,
				dataType : "json",
				success : pumadmin.saveSyncTaskDone,
				error : pumadmin.httpError
			});
			$("#saveSyncTaskSuccess").hide();
			$("#saveSyncTaskError").hide();
		},
		"saveSyncTaskDone" : function(data) {
			if (data.success == false) {
				$("#saveSyncTaskErrorCause").text(data.errorMsg);
				$("#saveSyncTaskError").show();
			} else {
				$("#saveSyncTaskSuccess").show();
				$("#saveSyncTaskButton").attr("disabled", "disabled");
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

/** 长度超过多少就截断，并加省略号 */
String.prototype.trunc = String.prototype.trunc ||
function(n){
    return this.length>n ? this.substr(0,n-3)+'...' : ""+this;
};

//UUID/Guid Generator
//use: UUID.create() or UUID.createSequential()
//convenience:  UUID.empty, UUID.tryParse(string)
(function(w){
//From http://baagoe.com/en/RandomMusings/javascript/
//Johannes BaagÃ¸e <baagoe@baagoe.com>, 2010
function Mash() {
var n = 0xefc8249d;

var mash = function(data) {
data = data.toString();
for (var i = 0; i < data.length; i++) {
 n += data.charCodeAt(i);
 var h = 0.02519603282416938 * n;
 n = h >>> 0;
 h -= n;
 h *= n;
 n = h >>> 0;
 h -= n;
 n += h * 0x100000000; // 2^32
}
return (n >>> 0) * 2.3283064365386963e-10; // 2^-32
};

mash.version = 'Mash 0.9';
return mash;
}

//From http://baagoe.com/en/RandomMusings/javascript/
function Kybos() {
return (function(args) {
// Johannes BaagÃ¸e <baagoe@baagoe.com>, 2010
var s0 = 0;
var s1 = 0;
var s2 = 0;
var c = 1;
var s = [];
var k = 0;

var mash = Mash();
var s0 = mash(' ');
var s1 = mash(' ');
var s2 = mash(' ');
for (var j = 0; j < 8; j++) {
 s[j] = mash(' ');
}

if (args.length == 0) {
 args = [+new Date];
}
for (var i = 0; i < args.length; i++) {
 s0 -= mash(args[i]);
 if (s0 < 0) {
 s0 += 1;
 }
 s1 -= mash(args[i]);
 if (s1 < 0) {
 s1 += 1;
 }
 s2 -= mash(args[i]);
 if (s2 < 0) {
 s2 += 1;
 }
 for (var j = 0; j < 8; j++) {
 s[j] -= mash(args[i]);
 if (s[j] < 0) {
   s[j] += 1;
 }
 }
}

var random = function() {
 var a = 2091639;
 k = s[k] * 8 | 0;
 var r = s[k];
 var t = a * s0 + c * 2.3283064365386963e-10; // 2^-32
 s0 = s1;
 s1 = s2;
 s2 = t - (c = t | 0);
 s[k] -= s2;
 if (s[k] < 0) {
 s[k] += 1;
 }
 return r;
};
random.uint32 = function() {
 return random() * 0x100000000; // 2^32
};
random.fract53 = function() {
 return random() +
 (random() * 0x200000 | 0) * 1.1102230246251565e-16; // 2^-53
};
random.addNoise = function() {
 for (var i = arguments.length - 1; i >= 0; i--) {
 for (j = 0; j < 8; j++) {
   s[j] -= mash(arguments[i]);
   if (s[j] < 0) {
   s[j] += 1;
   }
 }
 }
};
random.version = 'Kybos 0.9';
random.args = args;
return random;

} (Array.prototype.slice.call(arguments)));
};

var rnd = Kybos();
//UUID/GUID implementation from http://frugalcoder.us/post/2012/01/13/javascript-guid-uuid-generator.aspx
var UUID = {
"empty": "00000000-0000-0000-0000-000000000000"
,"parse": function(input) {
 var ret = input.toString().trim().toLowerCase().replace(/^[\s\r\n]+|[\{\}]|[\s\r\n]+$/g, "");
 if ((/[a-f0-9]{8}\-[a-f0-9]{4}\-[a-f0-9]{4}\-[a-f0-9]{4}\-[a-f0-9]{12}/).test(ret))
   return ret;
 else
   throw new Error("Unable to parse UUID");
}
,"createSequential": function() {
 var ret = new Date().valueOf().toString(16).replace("-","")
 for (;ret.length < 12; ret = "0" + ret);
 ret = ret.substr(ret.length-12,12); //only least significant part
 for (;ret.length < 32;ret += Math.floor(rnd() * 0xffffffff).toString(16));
 return [ret.substr(0,8), ret.substr(8,4), "4" + ret.substr(12,3), "89AB"[Math.floor(Math.random()*4)] + ret.substr(16,3),  ret.substr(20,12)].join("-");
}
,"create": function() {
 var ret = "";
 for (;ret.length < 32;ret += Math.floor(rnd() * 0xffffffff).toString(16));
 return [ret.substr(0,8), ret.substr(8,4), "4" + ret.substr(12,3), "89AB"[Math.floor(Math.random()*4)] + ret.substr(16,3),  ret.substr(20,12)].join("-");
}
,"random": function() {
 return rnd();
}
,"tryParse": function(input) {
 try {
   return UUID.parse(input);
 } catch(ex) {
   return UUID.empty;
 }
}
};
UUID["new"] = UUID.create;

w.UUID = w.Guid = UUID;
}(window || this));
