if($(".dirbrowse").children().length > 1){
  $(".fa-arrow-left").removeClass("disabled");
  //do something with forward arrow history
}

$(function() {

  var $contextMenu = $("#contextMenu");

  $("body").on("contextmenu", "div", function(e) {
    $contextMenu.css({
      display: "block",
      left: e.pageX,
      top: e.pageY
    });
    return false;
  });

  $contextMenu.on("click", "a", function() {
     $contextMenu.hide();
  });

});
