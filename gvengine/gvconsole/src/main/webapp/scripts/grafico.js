$(document).ready(function() {// Random data
      var line1 = new TimeSeries();
      setInterval(function() {
        line1.append(new Date().getTime(), Math.random());
      }, 1000);

      var smoothie = new SmoothieChart({ grid: { strokeStyle: 'rgb(185, 185, 185)', fillStyle: 'rgb(255, 255, 255)', lineWidth: 1, millisPerLine: 250, verticalSections: 6 } });
      smoothie.addTimeSeries(line1, { strokeStyle: 'rgb(35, 134, 80)', fillStyle: 'rgba(35, 134, 80, 0.4)', lineWidth: 3 });

      smoothie.streamTo(document.getElementById("mycanvas"), 1000);
})

$(document).ready(function() {// Random data
      var line1 = new TimeSeries();
      setInterval(function() {
        line1.append(new Date().getTime(), Math.random());
      }, 1000);

      var smoothie = new SmoothieChart({ grid: { strokeStyle: 'rgb(185, 185, 185)', fillStyle: 'rgb(255, 255, 255)', lineWidth: 1, millisPerLine: 250, verticalSections: 6 } });
      smoothie.addTimeSeries(line1, { strokeStyle: 'rgb(18, 70, 103)', fillStyle: 'rgba(18, 70, 103, 0.4)', lineWidth: 3 });

      smoothie.streamTo(document.getElementById("mycanvas2"), 1000);
})
