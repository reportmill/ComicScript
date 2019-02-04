
var CSMainArg0;
var CSMainArg1;

var ComicScript = {

 show: function show(anId, anObj) {

      CSMainArg0 = anId;
      CSMainArg1 = anObj!=null? JSON.stringify(anObj) : null;
      main();
  },

 showEditor: function showEditor(anId) {

      CSMainArg0 = "showEditor";
      CSMainArg1 = anId;
      main();
  }

};
