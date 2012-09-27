(function(w) {
	var timer;
	var puma_admin = {
		"next_step" : function() {
			var index = $("#stepsBreak > li[class='step_active']").index();
			index = index + 1;
			$("#stepsBreak > li[class='step_active']").attr("class","step_done");
			$("#stepsBreak > li:eq(" + index + ")").attr("class","step_active");
			$("#stepsCarousel").carousel('next');
		},
		"appError" : function(title, errorMsg) {
			rundemo_app.alertError(title, errorMsg);
		},
		"httpError" : function(xhr, textStatus, errorThrown) {
			rundemo_app.alertError('网络错误', 'error! (status:' + textStatus
					+ ',msg:' + errorThrown + ')');
		},
		"alertError" : function(title, errorMsg) {
			// 显示错误消息
			$('#errorMsg > div[class="modal-header"] > h3').text(title);
			$('#errorMsg > div[class="modal-body"] > p').text(errorMsg);
			$('#errorMsg').modal('show');
		}
	};
	w.puma_admin = puma_admin;
}(window || this));

$(document).ready(function() {

});