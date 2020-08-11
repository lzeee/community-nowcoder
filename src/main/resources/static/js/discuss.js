function like(btn, entityType, entityId, entityUserId){
    $.post(
        "/like",
        {"entityType":entityType, "entityId":entityId, "entityUserId":entityUserId},
        function (data) {
            data = $.parseJSON(data);
            if(data.code==0){
                //赞成功，修改按钮中b标签和i标签的内容
                $(btn).children("i").text(data.likeCount);
                $(btn).children("b").text(data.likeStatus==1?'已赞':'赞');
            }else{
                alert(data.msg);
            }
        }
    );
}