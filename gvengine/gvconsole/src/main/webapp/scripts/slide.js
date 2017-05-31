$(document).ready(function() {
  console.log("Ready");
  $('.navbar-toggle').click(function () {
		console.log("Click");
      $('.navbar-nav').toggleClass('slide-in');
      $('.side-body').toggleClass('body-slide-in');
  });
});
