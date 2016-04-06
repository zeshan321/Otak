var notificationArray = [];
var isNotiOpen = false;
var current;

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

function setText(s) {
	document.getElementById(current).innerHTML = s;
}

function addNotification(type) {
	current = type;
    notificationArray.push(type);
    showNotification();
}