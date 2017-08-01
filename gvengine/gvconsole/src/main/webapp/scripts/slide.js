angular.element(document).ready(function() {
  angular.element('.navbar-toggle').click(function () {
      angular.element('.navbar-nav').toggleClass('slide-in');
      angular.element('.side-body').toggleClass('body-slide-in');
  });
});
