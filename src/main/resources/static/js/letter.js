$(function () {
    $("#sendBtn").click(send_letter);
    $(".close-message").click(delete_msg);
    $(".close-notice").click(delete_notice);
});

function send_letter() {
    $("#sendModal").modal("hide");

    var toName = $("#recipient-name").val();
    var content = $("#message-text").val();

    // 发送AJAX请求之前，将CSRF令牌设置到请求的消息头中
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");
    $(document).ajaxSend(function (e, xhr, options) {
        xhr.setRequestHeader(header, token);
    });

    $.post(
        CONTEXT_PATH + "/letter/send",
        {"toName": toName, "content": content},
        function (data) {
            data = $.parseJSON(data);
            if (data.code === 0) {
                $("#hintBody").text("发送成功!");
            } else {
                $("#hintBody").text(data.msg);
            }

            $("#hintModal").modal("show");
            setTimeout(function () {
                $("#hintModal").modal("hide");
                location.reload();
            }, 2000);
        }
    );
}

function delete_msg() {
    var btn = this;
    var id = $(btn).prev().val();

    // 发送AJAX请求之前，将CSRF令牌设置到请求的消息头中
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");
    $(document).ajaxSend(function (e, xhr, options) {
        xhr.setRequestHeader(header, token);
    });

    $.post(
        CONTEXT_PATH + "/letter/delete",
        {"id": id},
        function (data) {
            data = $.parseJSON(data);
            if (data.code === 0) {
                $(btn).parents(".media").remove();
            } else {
                alert(data.msg);
            }
        }
    );
}

function delete_notice() {
    var btn = this;
    var id = $(btn).prev().val();

    // 发送AJAX请求之前，将CSRF令牌设置到请求的消息头中
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");
    $(document).ajaxSend(function (e, xhr, options) {
        xhr.setRequestHeader(header, token);
    });

    $.post(
        CONTEXT_PATH + "/notice/delete",
        {"id": id},
        function (data) {
            data = $.parseJSON(data);
            if (data.code === 0) {
                $(btn).parents(".media").remove();
            } else {
                alert(data.msg);
            }
        }
    );
}