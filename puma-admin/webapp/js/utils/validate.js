
var REGEXP_DB_NAME = /^\w{1,30}$/;

var validate = {

  do: function(value) {

  },

  dbName: function(name) {
    return REGEXP_DB_NAME.test(name);
  }

};