$(document).ready(function () {
    $("#memoryRead").html("<canvas id='memoryReadCanvas'/>");
    var g = new Bluff.Line('memoryReadCanvas', 600);

    g.title = 'In-memory read';

    g.data("BTreeMap", [ 137, 230, 410, 701, 1096]);
    g.data("ConcurrentSkipListMap", [ 93, 185, 361, 687, 1283]);
    g.labels = {0: '1', 1: '2 ', 2: '4', 3: '8', 4: '16', };

    g.y_axis_increment = 200;
    g.minimum_value = 0;
    g.x_axis_label = "number of threads";
    g.sort = false;
    g.theme_greyscale();
    g.draw();


});

$(document).ready(function () {
    $("#memoryUpdate").html("<canvas id='memoryUpdateCanvas'/>");
    var g = new Bluff.Line('memoryUpdateCanvas', 600);

    g.title = 'In-memory Update';

    g.data("BTreeMap", [ 104, 158, 249, 143, 126]);
    g.data("ConcurrentSkipListMap", [ 79, 151, 220, 401, 672]);
    g.labels = {0: '1', 1: '2 ', 2: '4', 3: '8', 4: '16', };

    g.y_axis_increment = 200;
    g.minimum_value = 0;
    g.x_axis_label = "number of threads";
    g.sort = false;
    g.theme_greyscale();
    g.draw();

});



$(document).ready(function () {
    $("#memoryCombined").html("<canvas id='memoryCombinedCanvas'/>");
    var g = new Bluff.Line('memoryCombinedCanvas', 600);

    g.title = 'In-memory Update&Read';

    g.data("BTreeMap", [ 124, 207, 364, 562, 352]);
    g.data("ConcurrentSkipListMap", [ 88, 166, 301, 542, 1056]);
    g.labels = {0: '1', 1: '2 ', 2: '4', 3: '8', 4: '16', };

    g.y_axis_increment = 200;
    g.minimum_value = 0;
    g.x_axis_label = "number of threads";
    g.sort = false;
    g.theme_greyscale();
    g.draw();

});




























