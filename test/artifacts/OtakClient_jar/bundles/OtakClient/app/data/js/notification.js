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
