$(document).ready(function() {
  var table = $('#dependenciestable').DataTable({
                                                  responsive: true,
                                                  "columnDefs": [
                                                    { "visible": false, "targets": 0 },
                                                    { "orderable": false, "targets": ["_all"]}
                                                  ],
                                                  "order": [[ 0, 'asc' ]],
                                                  "displayLength": 25,
                                                  columns: [
                                                    null,
                                                    null,
                                                    {
                                                      "render": function(data, type, row){
                                                        return data.split(", ").join("<br/>");
                                                      }
                                                    }
                                                  ],
                                                  "drawCallback": function ( settings ) {
                                                    var api = this.api();
                                                    var rows = api.rows( {page:'current'} ).nodes();
                                                    var last=null;

                                                    api.column(0, {page:'current'} ).data().each( function ( group, i ) {
                                                      if ( last !== group ) {
                                                        $(rows).eq( i ).before(
                                                            '<tr class="group"><td colspan="5">'+group+'</td></tr>'
                                                        );

                                                        last = group;
                                                      }
                                                    } );
                                                  }
                                                } );

  // Order by the grouping
  $('#dependenciestable tbody').on( 'click', 'tr.group', function () {
    var currentOrder = table.order()[0];
    if ( currentOrder[0] === 2 && currentOrder[1] === 'asc' ) {
      table.order( [ 0, 'desc' ] ).draw();
    }
    else {
      table.order( [ 0, 'asc' ] ).draw();
    }
  } );
} );