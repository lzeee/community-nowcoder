$(function(){
	$("#publishBtn").click(publish);
});

function publish() {
	$("#publishModal").modal("hide");
	//异步请求发送帖子标题和内容
	var title = $("#recipient-name").val();
	var content = $("#message-text").val();
	$.post(
		"/discuss/add",
		{"title":title, "content":content},
		function(data){
			data = $.parseJSON(data);
			//在提示框当中显示返回的消息
			$("#hintBody").text(data.msg);
			//显示提示框
			$("#hintModal").modal("show");
			//两秒后自动隐藏提示框
			setTimeout(function(){
				$("#hintModal").modal("hide");
				//如果帖子发布成功刷新页面
				if(data.code==0){
					window.location.reload();
				}
			}, 2000);
		}
	);

}