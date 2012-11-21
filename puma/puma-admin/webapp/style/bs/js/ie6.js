$(function() {
	if ($.browser.msie && parseInt($.browser.version, 10) === 6) {
		$('.row div[class^="span"]:last-child').addClass('last-child');
        $('[class*="span"]').addClass('margin-left-20');
        $('[class*="span"][class*="offset"]').removeClass('margin-left-20');
        $(':button[class="btn"], :reset[class="btn"], :submit[class="btn"], input[type="button"]').addClass('button-reset');
        $(':checkbox').addClass('input-checkbox');
        $('[class^="icon-"], [class*=" icon-"]').addClass('icon-sprite');
        $('.pagination li:first-child a').addClass('pagination-first-child');
	}
	alert('您使用的是IE6浏览器，过于陈旧，为了网络安全，建议升级到IE7/8/9/10，或更换firefox或chrome浏览器！');
});