$(function () {
    $("#topBtn").click(setTop);
    $("#wonderfulBtn").click(setWonderful);
    $("#deleteBtn").click(setDelete);
});


function like(btn, entityType, entityId, entityUserId, postId){
    $.post(
        "/like",
        {"entityType":entityType, "entityId":entityId, "entityUserId":entityUserId, "postId":postId},
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

function setTop() {
    $.post(
        "/discuss/top",
        {"id":$("#postId").val()},
        function (data) {
            data = $.parseJSON(data);
            if(data.code == 0){
                $("#topBtn").attr("disabled", "disabled");
            }else{
                alert(data.msg);
            }
        }
    );
}
//加精
function setWonderful() {
    $.post(
        "/discuss/wonderful",
        {"id":$("#postId").val()},
        function (data) {
            data = $.parseJSON(data);
            if(data.code == 0){
                $("#wonderfulBtn").attr("disabled", "disabled");
            }else{
                alert(data.msg);
            }
        }
    );
}
//删除
function setDelete() {
    $.post(
        "/discuss/delete",
        {"id":$("#postId").val()},
        function (data) {
            data = $.parseJSON(data);
            if(data.code == 0){
                //删除完跳转就行了
                location.href= "/index";
            }else{
                alert(data.msg);
            }
        }
    );
}


