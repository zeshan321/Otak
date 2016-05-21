var notificationArray = [];
var isNotiOpen = false;

function showNotification() {
    if (isNotiOpen) {
        return;
    }

	nextNoti = notificationArray[0];
    isNotiOpen = true;

    $('.notification.' + nextNoti).removeClass('bounceOutRight notification-show animated bounceInRight');
    $('.notification.' + nextNoti).addClass('notification-show animated bounceInRight');

    $('.notification.' + nextNoti).one('webkitAnimationEnd mozAnimationEnd MSAnimationEnd oanimationend animationend', function() {
        setTimeout(function() {
            $('.notification.' + nextNoti).addClass('animated bounceOutRight');
            setTimeout(function() {
                notificationArray.shift();

                isNotiOpen = false;
                if (notificationArray.length > 0) {
                    showNotification();
                }

            }, 1000);
        }, 3000);
    });
}

function addNotification(type) {
    if (notificationArray.indexOf(type) == -1){
      notificationArray.push(type);
    }
    showNotification();
}

//warning for limit
function warnLimit(){
  var checkbox = $("#overridelim");
  var oldval
  if(checkbox.is(':checked')){
      $("#dllim").removeAttr("max");
      checkbox.parent().parent().append("<span id='warnlim' style='color:#C9302C; font-size: 10px;'><br><i class='fa fa-exclamation-circle' aria-hidden='true'></i> \
      Values greater than the recommended maximum may slow down the performance of Otak.</span>");
  } else {
      $("#dllim").attr("max", 10);
      if($("#dllim").val() > 10){
        $("#dllim").val(10);
      }
      checkbox.parent().parent().find("span").remove();
  }
}
