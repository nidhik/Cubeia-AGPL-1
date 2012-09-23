"use strict";
var Poker = Poker || {};

Poker.LobbyLayoutManager = Class.extend({
    lobbyData : [],
    listItemTemplate : null,
    filters : [],
    currentScroll : null,
    init : function() {
        var templateManager = new Poker.TemplateManager();
        this.listItemTemplate = templateManager.getTemplate("tableListItemTemplate");
        var self = this;

        $("#cashGameMenu").click(function(e){
            $(".main-menu .selected").removeClass("selected");
            $(this).addClass("selected");
        });
        $("#sitAndGoMenu").click(function(e){
            $(".main-menu .selected").removeClass("selected");
            $(this).addClass("selected");
        });
        $("#tournamentMenu").click(function(e){
            $(".main-menu .selected").removeClass("selected");
            $(this).addClass("selected");
        });
        this.initFilters();
    },
    initFilters : function() {
        var fullTablesFilter = new Poker.LobbyFilter("fullTables",true,
            function(enabled,lobbyData){
                if(!enabled) {
                    return lobbyData.seated<lobbyData.capacity;
                } else {
                    return true;
                }
            },this);
        this.filters.push(fullTablesFilter);
        var emptyTablesFilter = new Poker.LobbyFilter("emptyTables",true,
            function(enabled,lobbyData){
                if(!enabled) {
                    return lobbyData.seated>0;
                } else {
                    return true;
                }

            },this);

        this.filters.push(emptyTablesFilter);


        var noLimit = new Poker.PropertyStringFilter("noLimit",true,this,"type","NL");
        this.filters.push(noLimit);

        var potLimit = new Poker.PropertyStringFilter("potLimit",true,this,"type","PL");
        this.filters.push(potLimit);

        var fixedLimit = new Poker.PropertyStringFilter("fixedLimit",true,this,"type","FL");
        this.filters.push(fixedLimit);

        var highStakes = new Poker.PropertyMinMaxFilter("highStakes",true,this,"ante",1000,-1);

        this.filters.push(highStakes);

        var mediumStakes = new Poker.PropertyMinMaxFilter("mediumStakes",true,this,"ante",50,1000);
        this.filters.push(mediumStakes);

        var lowStakes = new Poker.PropertyMinMaxFilter("lowStakes",true,this,"ante",-1,50);
        this.filters.push(lowStakes);
    },
    handleTableSnapshotList: function(tableSnapshotList) {
        for (var i = 0; i < tableSnapshotList.length; i ++) {
            this.handleTableSnapshot(tableSnapshotList[i]);
        }
        this.createGrid();
    },
    handleTableSnapshot : function(tableSnapshot) {
        if (this.findTable(tableSnapshot.tableid) === null) {

            var speedParam = this.readParam("SPEED", tableSnapshot.params);
            //var variant = this.readParam("VARIANT", tableSnapshot.params);
            var bettingModel = this.readParam("BETTING_GAME_BETTING_MODEL",tableSnapshot.params);
            var ante = this.readParam("BETTING_GAME_ANTE",tableSnapshot.params);

            var data = {id:tableSnapshot.tableid,
                name:tableSnapshot.name,
                speed:speedParam,
                capacity: tableSnapshot.capacity,
                seated: tableSnapshot.seated,
                blinds : (Poker.Utils.formatBlinds(ante)+"/"+Poker.Utils.formatBlinds(ante*2)),
                type : this.getBettingModel(bettingModel),
                tableStatus : this.getTableStatus(tableSnapshot.seated,tableSnapshot.capacity),
                ante : ante
            };
            var i = this.lobbyData.push(data);

        } else {
            console.log("duplicate found - tableid: " + tableSnapshot.tableid);
        }
    },
    getTableStatus : function(seated,capacity) {
        if(seated == capacity) {
            return "full";
        }
        return "open";
    },
    getBettingModel : function(model) {
       if(model == "NO_LIMIT") {
           return "NL"
       } else if (model == "POT_LIMIT"){
           return "PL";
       } else if(model == "FIXED_LIMIT") {
           return "FL";
       }
       return model;
    },
    handleTableUpdateList : function(tableUpdateList) {
        for (var i = 0; i < tableUpdateList.length; i ++) {
            this.handleTableUpdate(tableUpdateList[i]);
        }

    },
    handleTableUpdate : function(tableUpdate) {
        var tableData = this.findTable(tableUpdate.tableid);
        if (tableData) {
            if(tableData.seated ==  tableData.seated) {
                console.log("on update, seated players the same, skipping update");
                return;
            }
            tableData.seated = tableUpdate.seated;
            //it might be filtered out
            var item = $("#tableItem"+tableData.id);
            if(item.length>0) {
                item.unbind().replaceWith(this.getTableItemHtml(tableData));
                var item = $("#tableItem"+tableData.id);  //need to pick it up again to be able to bind to it
                item.click(function(e){
                    comHandler.openTable(tableData.id,tableData.capacity);
                });
            }
            console.log("table " + tableData.id +"  updated, seated = "+ tableData.seated);
        }
    },
    handleTableRemoved : function(tableid) {
        console.debug("removing table " + tableid);
        this.removeTable(tableid);
        $("#tableItem"+tableid).remove();

    },
    removeTable : function(tableid) {
        for (var i = 0; i < this.lobbyData.length; i ++) {
            var object = this.lobbyData[i];
            if (object.id == tableid) {
                this.lobbyData.splice(i, 1);
                return;
            }
        }
    },

    findTable : function(tableid) {
        for (var i = 0; i < this.lobbyData.length; i ++) {
            var object = this.lobbyData[i];
            if (object.id == tableid) {
                return object;
            }
        }
        return null;
    },
    reSort : function() {

    },

    createGrid : function() {

        $('#lobby').show();
        $("#tableListItemContainer").empty();

        var self = this;
        var count = 0;
        $.each(this.lobbyData,function(i,data){
            if(self.includeData(data)) {
                count++;
                $("#tableListItemContainer").append(self.getTableItemHtml(data));
                $("#tableItem"+data.id).click(function(e){
                    $("#tableListItemContainer").empty();
                    comHandler.openTable(data.id,data.capacity);
                });
            }
        });
        if(count==0) {
            $("#tableListItemContainer").append($("<div/>").addClass("no-tables").html("Currently no tables matching your criteria"));
        }

        setTimeout(function(){
            self.currentScroll = $("#tableListContainerWrapper").niceScroll("#tableListContainer",
                {cursorcolor:"#555", scrollspeed:50, bouncescroll : false, cursorwidth : 8, cursorborder : "none"});
            if(count==0) {
                self.currentScroll.scrollTop(0);
            }
        },300);

        console.debug("grid created");
    },
    includeData : function(tableData) {
      for(var i = 0; i<this.filters.length; i++) {
          var filter = this.filters[i];
          if(filter.filter(tableData) == false) {
              return false;
          }
      }
      return true;
    },
    getTableItemHtml : function(t) {
        var item =  Mustache.render(this.listItemTemplate,t);
        return item;
    },
    readParam : function(key, params) {

        for (var i = 0; i < params.length; i ++) {
            var object = params[i];

            if (object.key == key) {
                //console.log("'"+object.key+"' val = " + object.value);
                //var valueArray = FIREBASE.ByteArray.fromBase64String(object);
                //console.log(object);

                var p = null;
                var valueArray =  FIREBASE.ByteArray.fromBase64String(object.value);
                var byteArray = new FIREBASE.ByteArray(valueArray);
                if ( object.type == 1 ) {
                    p =  byteArray.readInt();
                } else {
                    p =  byteArray.readString();
                }

                //shouldn't this work?
                //  var p =  FIREBASE.Styx.readParam(object);
                return p;
            }
        }
    },
    getCapacity : function(id) {
        var tableData = this.findTable(id);
        return tableData.capacity;
    },
    showLogin : function() {
        $('#dialog1').fadeIn(1000);

    }
});

Poker.LobbyFilter = Class.extend({
    enabled : false,
    id : null,
    filterFunction : null,
    lobbyLayoutManager : null,
    init : function(id,enabled,filterFunction,lobbyLayoutManager) {
        this.enabled = Poker.Utils.loadBoolean(id,true);

        this.id = id;
        this.filterFunction = filterFunction;
        this.lobbyLayoutManager = lobbyLayoutManager;
        var self = this;

        $("#"+id).click(function(){
            self.enabled=!self.enabled;
            $(this).toggleClass("active");
            Poker.Utils.store(self.id,self.enabled);
            self.filterUpdated();

        });
        if(this.enabled==true) {
            $("#"+this.id).addClass("active");
        } else {
            $("#"+this.id).removeClass("active");
        }
    },
    filterUpdated : function(){
        this.lobbyLayoutManager.createGrid();
    },
    /**
     * Returns true if it should be included in the lobby and
     * false if it shouldn't
     * @param lobbyData
     * @return {boolean} if it should be included
     */
    filter : function(lobbyData) {
        return this.filterFunction(this.enabled,lobbyData);
    }
});

Poker.PropertyMinMaxFilter = Poker.LobbyFilter.extend({
    min :-1,
    max : -1,
    property : null,
    init : function(id,enabled,lobbyLayoutManager,property,min,max) {
        this.min = min;
        this.max = max;
        this.property = property;
        var self = this;
        this._super(id,enabled,function(enabled,lobbyData){
            return self.doFilter(enabled,lobbyData);
        },lobbyLayoutManager);

    },
    doFilter : function(enabled, lobbyData) {
        var p = lobbyData[this.property];
        if(typeof(p)!="undefined" && !this.enabled) {
            if(this.max!=-1 && this.min!=-1) {
                return p > this.max || p < this.min;
            } else if(this.max!=-1) {
                return p>this.max;
            } else if(this.min!=-1) {
                return p<this.min;
            } else {
                console.log("PropertyFilter: neither min or max is defined");
                return true;
            }
        } else {
            return true;
        }
    }
});

Poker.PropertyStringFilter = Poker.LobbyFilter.extend({
    str : null,
    property : null,
    init : function(id,enabled,lobbyLayoutManager,property,str) {
        this.property = property;
        this.str = str;
        var self = this;
        this._super(id,enabled,function(enabled,lobbyData){
            return self.doFilter(enabled,lobbyData);
        },lobbyLayoutManager);

    },
    doFilter : function(enabled, lobbyData) {
        var p = lobbyData[this.property];
        if(typeof(p)!="undefined" && !this.enabled) {
            if(p !== this.str) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }
});





