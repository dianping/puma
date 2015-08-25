puma.config(function ($httpProvider) {
    $httpProvider.interceptors.push(function ($q) {
        return {
            response: function (response) {
                return response;
            },
            responseError: function (responseError) {
                if (responseError.status === 500) {
                    alert('failure');
                }
                return $q.reject(responseError);
            }
        };
    });
});

puma.controller('pumaCreateController', function ($scope, $http) {

    $scope.$on('$viewContentLoaded', function () {
        jQuery(function ($) {
            $('#id-disable-check').on('click', function () {
                var inp = $('#form-input-readonly').get(0);
                if (inp.hasAttribute('disabled')) {
                    inp.setAttribute('readonly', 'true');
                    inp.removeAttribute('disabled');
                    inp.value = "This text field is readonly!";
                }
                else {
                    inp.setAttribute('disabled', 'disabled');
                    inp.removeAttribute('readonly');
                    inp.value = "This text field is disabled!";
                }
            });


            $('.chosen-select').chosen({allow_single_deselect: true});
            //resize the chosen on window resize

            $(window)
                .off('resize.chosen')
                .on('resize.chosen', function () {
                    $('.chosen-select').each(function () {
                        var $this = $(this);
                        $this.next().css({'width': $this.parent().width()});
                    })
                }).trigger('resize.chosen');


            $('#chosen-multiple-style').on('click', function (e) {
                var target = $(e.target).find('input[type=radio]');
                var which = parseInt(target.val());
                if (which == 2) $('#form-field-select-4').addClass('tag-input-style');
                else $('#form-field-select-4').removeClass('tag-input-style');
            });


            $('[data-rel=tooltip]').tooltip({container: 'body'});
            $('[data-rel=popover]').popover({container: 'body'});

            $('textarea[class*=autosize]').autosize({append: "\n"});
            $('textarea.limited').inputlimiter({
                remText: '%n character%s remaining...',
                limitText: 'max allowed : %n.'
            });

            $.mask.definitions['~'] = '[+-]';
            $('.input-mask-date').mask('99/99/9999');
            $('.input-mask-phone').mask('(999) 999-9999');
            $('.input-mask-eyescript').mask('~9.99 ~9.99 999');
            $(".input-mask-product").mask("a*-999-a999", {
                placeholder: " ", completed: function () {
                    alert("You typed the following: " + this.val());
                }
            });


            $("#input-size-slider").css('width', '200px').slider({
                value: 1,
                range: "min",
                min: 1,
                max: 8,
                step: 1,
                slide: function (event, ui) {
                    var sizing = ['', 'input-sm', 'input-lg', 'input-mini', 'input-small', 'input-medium', 'input-large', 'input-xlarge', 'input-xxlarge'];
                    var val = parseInt(ui.value);
                    $('#form-field-4').attr('class', sizing[val]).val('.' + sizing[val]);
                }
            });

            $("#input-span-slider").slider({
                value: 1,
                range: "min",
                min: 1,
                max: 12,
                step: 1,
                slide: function (event, ui) {
                    var val = parseInt(ui.value);
                    $('#form-field-5').attr('class', 'col-xs-' + val).val('.col-xs-' + val);
                }
            });


            //"jQuery UI Slider"
            //range slider tooltip example
            $("#slider-range").css('height', '200px').slider({
                orientation: "vertical",
                range: true,
                min: 0,
                max: 100,
                values: [17, 67],
                slide: function (event, ui) {
                    var val = ui.values[$(ui.handle).index() - 1] + "";

                    if (!ui.handle.firstChild) {
                        $("<div class='tooltip right in' style='display:none;left:16px;top:-6px;'><div class='tooltip-arrow'></div><div class='tooltip-inner'></div></div>")
                            .prependTo(ui.handle);
                    }
                    $(ui.handle.firstChild).show().children().eq(1).text(val);
                }
            }).find('a').on('blur', function () {
                $(this.firstChild).hide();
            });


            $("#slider-range-max").slider({
                range: "max",
                min: 1,
                max: 10,
                value: 2
            });

            $("#slider-eq > span").css({width: '90%', 'float': 'left', margin: '15px'}).each(function () {
                // read initial values from markup and remove that
                var value = parseInt($(this).text(), 10);
                $(this).empty().slider({
                    value: value,
                    range: "min",
                    animate: true

                });
            });

            $("#slider-eq > span.ui-slider-purple").slider('disable');//disable third item


            $('#id-input-file-1 , #id-input-file-2').ace_file_input({
                no_file: 'No File ...',
                btn_choose: 'Choose',
                btn_change: 'Change',
                droppable: false,
                onchange: null,
                thumbnail: false //| true | large
                //whitelist:'gif|png|jpg|jpeg'
                //blacklist:'exe|php'
                //onchange:''
                //
            });
            //pre-show a file name, for example a previously selected file
            //$('#id-input-file-1').ace_file_input('show_file_list', ['myfile.txt'])


            $('#id-input-file-3').ace_file_input({
                style: 'well',
                btn_choose: 'Drop files here or click to choose',
                btn_change: null,
                no_icon: 'ace-icon fa fa-cloud-upload',
                droppable: true,
                thumbnail: 'small'//large | fit
                //,icon_remove:null//set null, to hide remove/reset button
                /**,before_change:function(files, dropped) {
						//Check an example below
						//or examples/file-upload.html
						return true;
					}*/
                /**,before_remove : function() {
						return true;
					}*/
                ,
                preview_error: function (filename, error_code) {
                    //name of the file that failed
                    //error_code values
                    //1 = 'FILE_LOAD_FAILED',
                    //2 = 'IMAGE_LOAD_FAILED',
                    //3 = 'THUMBNAIL_FAILED'
                    //alert(error_code);
                }

            }).on('change', function () {
                //console.log($(this).data('ace_input_files'));
                //console.log($(this).data('ace_input_method'));
            });


            //dynamically change allowed formats by changing allowExt && allowMime function
            $('#id-file-format').removeAttr('checked').on('change', function () {
                var whitelist_ext, whitelist_mime;
                var btn_choose
                var no_icon
                if (this.checked) {
                    btn_choose = "Drop images here or click to choose";
                    no_icon = "ace-icon fa fa-picture-o";

                    whitelist_ext = ["jpeg", "jpg", "png", "gif", "bmp"];
                    whitelist_mime = ["image/jpg", "image/jpeg", "image/png", "image/gif", "image/bmp"];
                }
                else {
                    btn_choose = "Drop files here or click to choose";
                    no_icon = "ace-icon fa fa-cloud-upload";

                    whitelist_ext = null;//all extensions are acceptable
                    whitelist_mime = null;//all mimes are acceptable
                }
                var file_input = $('#id-input-file-3');
                file_input
                    .ace_file_input('update_settings',
                    {
                        'btn_choose': btn_choose,
                        'no_icon': no_icon,
                        'allowExt': whitelist_ext,
                        'allowMime': whitelist_mime
                    })
                file_input.ace_file_input('reset_input');

                file_input
                    .off('file.error.ace')
                    .on('file.error.ace', function (e, info) {
                        //console.log(info.file_count);//number of selected files
                        //console.log(info.invalid_count);//number of invalid files
                        //console.log(info.error_list);//a list of errors in the following format

                        //info.error_count['ext']
                        //info.error_count['mime']
                        //info.error_count['size']

                        //info.error_list['ext']  = [list of file names with invalid extension]
                        //info.error_list['mime'] = [list of file names with invalid mimetype]
                        //info.error_list['size'] = [list of file names with invalid size]


                        /**
                         if( !info.dropped ) {
							//perhapse reset file field if files have been selected, and there are invalid files among them
							//when files are dropped, only valid files will be added to our file array
							e.preventDefault();//it will rest input
						}
                         */


                        //if files have been selected (not dropped), you can choose to reset input
                        //because browser keeps all selected files anyway and this cannot be changed
                        //we can only reset file field to become empty again
                        //on any case you still should check files with your server side script
                        //because any arbitrary file can be uploaded by user and it's not safe to rely on browser-side measures
                    });

            });

            $('#spinner1').ace_spinner({
                value: 0,
                min: 0,
                max: 200,
                step: 10,
                btn_up_class: 'btn-info',
                btn_down_class: 'btn-info'
            })
                .on('change', function () {
                    //alert(this.value)
                });
            $('#spinner2').ace_spinner({
                value: 0,
                min: 0,
                max: 10000,
                step: 100,
                touch_spinner: true,
                icon_up: 'ace-icon fa fa-caret-up',
                icon_down: 'ace-icon fa fa-caret-down'
            });
            $('#spinner3').ace_spinner({
                value: 0,
                min: -100,
                max: 100,
                step: 10,
                on_sides: true,
                icon_up: 'ace-icon fa fa-plus smaller-75',
                icon_down: 'ace-icon fa fa-minus smaller-75',
                btn_up_class: 'btn-success',
                btn_down_class: 'btn-danger'
            });
            //$('#spinner1').ace_spinner('disable').ace_spinner('value', 11);
            //or
            //$('#spinner1').closest('.ace-spinner').spinner('disable').spinner('enable').spinner('value', 11);//disable, enable or change value
            //$('#spinner1').closest('.ace-spinner').spinner('value', 0);//reset to 0


            //datepicker plugin
            //link
            $('.date-picker').datepicker({
                autoclose: true,
                todayHighlight: true
            })
                //show datepicker when clicking on the icon
                .next().on(ace.click_event, function () {
                    $(this).prev().focus();
                });

            //or change it into a date range picker
            $('.input-daterange').datepicker({autoclose: true});


            //to translate the daterange picker, please copy the "examples/daterange-fr.js" contents here before initialization
            $('input[name=date-range-picker]').daterangepicker({
                'applyClass': 'btn-sm btn-success',
                'cancelClass': 'btn-sm btn-default',
                locale: {
                    applyLabel: 'Apply',
                    cancelLabel: 'Cancel',
                }
            })
                .prev().on(ace.click_event, function () {
                    $(this).next().focus();
                });


            $('#timepicker1').timepicker({
                minuteStep: 1,
                showSeconds: true,
                showMeridian: false
            }).next().on(ace.click_event, function () {
                $(this).prev().focus();
            });

            $('#form-begin-time').datetimepicker().next().on(ace.click_event, function () {
                $(this).prev().focus();
            });


            $('#colorpicker1').colorpicker();

            $('#simple-colorpicker-1').ace_colorpicker();
            //$('#simple-colorpicker-1').ace_colorpicker('pick', 2);//select 2nd color
            //$('#simple-colorpicker-1').ace_colorpicker('pick', '#fbe983');//select #fbe983 color
            //var picker = $('#simple-colorpicker-1').data('ace_colorpicker')
            //picker.pick('red', true);//insert the color if it doesn't exist


            $(".knob").knob();


            var tag_input = $('#form-field-tags');
            try {
                tag_input.tag(
                    {
                        placeholder: tag_input.attr('placeholder'),
                        //enable typeahead by specifying the source array
                        source: ace.vars['US_STATES'],//defined in ace.js >> ace.enable_search_ahead
                        /**
                         //or fetch data from database, fetch those that match "query"
                         source: function(query, process) {
						  $.ajax({url: 'remote_source.php?q='+encodeURIComponent(query)})
						  .done(function(result_items){
							process(result_items);
						  });
						}
                         */
                    }
                )

                //programmatically add a new
                var $tag_obj = $('#form-field-tags').data('tag');
                $tag_obj.add('Programmatically Added');
            }
            catch (e) {
                //display a textarea for old IE, because it doesn't support this plugin or another one I tried!
                tag_input.after('<textarea id="' + tag_input.attr('id') + '" name="' + tag_input.attr('name') + '" rows="3">' + tag_input.val() + '</textarea>').remove();
                //$('#form-field-tags').autosize({append: "\n"});
            }


            //////////

            //typeahead.js
            //example taken from plugin's page at: https://twitter.github.io/typeahead.js/examples/
            var substringMatcher = function (strs) {
                return function findMatches(q, cb) {
                    var matches, substringRegex;

                    // an array that will be populated with substring matches
                    matches = [];

                    // regex used to determine if a string contains the substring `q`
                    substrRegex = new RegExp(q, 'i');

                    // iterate through the pool of strings and for any string that
                    // contains the substring `q`, add it to the `matches` array
                    $.each(strs, function (i, str) {
                        if (substrRegex.test(str)) {
                            // the typeahead jQuery plugin expects suggestions to a
                            // JavaScript object, refer to typeahead docs for more info
                            matches.push({value: str});
                        }
                    });

                    cb(matches);
                }
            }

            $('input.typeahead').typeahead({
                hint: true,
                highlight: true,
                minLength: 1
            }, {
                name: 'states',
                displayKey: 'value',
                source: substringMatcher(ace.vars['US_STATES'])
            });


            /////////
            $('#modal-form input[type=file]').ace_file_input({
                style: 'well',
                btn_choose: 'Drop files here or click to choose',
                btn_change: null,
                no_icon: 'ace-icon fa fa-cloud-upload',
                droppable: true,
                thumbnail: 'large'
            })

            //chosen plugin inside a modal will have a zero width because the select element is originally hidden
            //and its width cannot be determined.
            //so we set the width after modal is show
            $('#modal-form').on('shown.bs.modal', function () {
                $(this).find('.chosen-container').each(function () {
                    $(this).find('a:first-child').css('width', '210px');
                    $(this).find('.chosen-drop').css('width', '210px');
                    $(this).find('.chosen-search input').css('width', '200px');
                });
            })
            /**
             //or you can activate the chosen plugin after modal is shown
             //this way select element becomes visible with dimensions and chosen works as expected
             $('#modal-form').on('shown', function () {
					$(this).find('.modal-chosen').chosen();
				})
             */
        });
    });


    $scope.inputDatabases = [];
    $scope.outputDatabase = undefined;
    (function () {
        $http.get('/a/puma-create/database').success(function (inputDatabases) {
            $scope.inputDatabases = inputDatabases;
            jQuery(function ($) {
                $("#form-database").autocomplete({
                    source: $scope.inputDatabases
                });
            });
        });
    })();

    $scope.inputTables = [];
    $scope.outputTables = [];
    $scope.findTables = function () {
        jQuery(function ($) {
            $scope.outputDatabase = $("#form-database").val()
        });
        if ($scope.outputDatabase !== undefined) {
            var url = '/a/puma-create/table?database=' + $scope.outputDatabase;
            $http.get(url).success(function (tables) {
                angular.forEach(tables, function (table) {
                    $scope.inputTables.push({name: table});
                });
            });
        }
    };

    $scope.inputServers = [];
    $scope.outputServers = [];
    (function () {
        $http.get('/a/puma-create/server').success(function (servers) {
            angular.forEach(servers, function (server) {
                $scope.inputServers.push({name: server});
            });
        });
    })();

    $scope.submit = function () {
        jQuery(function ($) {
            $scope.outputDatabase = $("#form-database").val();
            $scope.outputBeginTime = $("#form-begin-time").val();
            $scope.outputBeginTime = moment($scope.outputBeginTime).format();
        });

        var tables = [];
        angular.forEach($scope.outputTables, function (table) {
            tables.push(table['name']);
        });
        var servers = [];
        angular.forEach($scope.outputServers, function (server) {
            servers.push(server['name']);
        });
        var database = $scope.outputDatabase;

        var beginTimes = {};
        angular.forEach(servers, function (server) {
            beginTimes[server] = $scope.outputBeginTime;
        });

        var json = {
            database: database,
            tables: tables,
            serverNames: servers,
            beginTimes: beginTimes
        };

        $http.post('/a/puma-create', json);
    };

    $scope.reset = function () {
        $scope.database = null;
        $scope.tables = null;
        $scope.servers = null;
        $scope.beginTime = null;
    }
});

puma.controller('pumaTargetController', function ($scope, $http) {

    $scope.pumaDto = {};

    $scope.allServers = [];
    $scope.allTables = [];
    $scope.createMode = false;

    $scope.showCreateTables = false;


    (function () {
        $http.get('/a/puma-create/server').success(function (servers) {
            angular.forEach(servers, function (server) {
                $scope.allServers.push(server);
            });
        });
    })();

    $scope.search = function () {
        var url = '/a/puma/search?database=' + $scope.pumaDto.database;
        $http.get(url).success(
            function (response) {
                $scope.pumaDto = response;

                $scope.servers = [];
                angular.forEach($scope.pumaDto.serverNames, function(serverName) {
                    var server = {
                        name: serverName,
                        input: buildInput(serverName, $scope.pumaDto.serverNames),
                        output: []
                    };
                    $scope.servers.push(server);
                });

                $scope.findTables();
                if (_.size($scope.servers) === 0) {
                    $scope.add();
                }
            }
        );
    };

    $scope.findTables = function () {
        $scope.allTables = [];
        var database = $scope.pumaDto.database;
        var tables = $scope.pumaDto.tables;
        var url = '/a/puma-create/table?database=' + database;
        $http.get(url).success(function (allTables) {
            angular.forEach(allTables, function (table) {
                if (_.contains(tables, table)) {
                    $scope.allTables.push({name: table, selected: true});
                } else {
                    $scope.allTables.push({name: table});
                }
            });
        });
    };

    function buildInput(serverName, allServers) {
        var list = [];
        angular.forEach(allServers, function(server) {
            if (server === serverName) {
                list.push({ name: server, selected: true });
            } else {
                list.push({ name: server });
            }
        });
        return list;
    };

    function parseOutput(servers) {
        var list = [];
        angular.forEach(servers, function(server) {
             list.push(server.output[0].name);
        });
        return list;
    };

    function parseTables(tables) {
        var list = [];
        angular.forEach(tables, function(table) {
            list.push(table.name);
        });
        return list;
    }

    $scope.submit = function() {
        $scope.pumaDto.serverNames = parseOutput($scope.servers);
        $scope.pumaDto.tables = parseTables($scope.pumaDto.tables);

        var json = {
            serverNames: $scope.pumaDto.serverNames,
            tables: $scope.pumaDto.tables,
            database: $scope.pumaDto.database
        };

        $http.post('/a/puma-create', json).success(function(response) {
            alert('success');
        });
    };

    $scope.add = function() {
        $scope.servers.push({ name: '', input: buildInput('', $scope.allServers), output: [] });
    };

    $scope.delete = function(server) {
        var index = $scope.servers.indexOf(server);
        $scope.servers.splice(index, 1);
    }

    $scope.checkShowWell = function() {
        return _.size($scope.pumaDto.serverNames) === 0 && !$scope.createMode;
    };

    $scope.checkShowNewServers = function() {
        return $scope.createMode;
    };

    $scope.checkShowOldServers = function() {
        return !$scope.createMode;
    };
});