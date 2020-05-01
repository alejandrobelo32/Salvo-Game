$(function () {
  loadData();
});

function getParameterByName(name) {
  var match = RegExp('[?&]' + name + '=([^&]*)').exec(window.location.search);
  return match && decodeURIComponent(match[1].replace(/\+/g, ' '));
};

function loadData() {  

  $.get('/api/game_view/' + getParameterByName('gp'))

    .done(function (data) {

      if (data.ships.length == 0){
        //location.href = "placeShips.html?gp=" + getParameterByName('gp');
        $('#placeShips').show();
    
      }else{

        $('#tables').show();

      }

      console.log(data)

      let playerInfo = [];

      data.gamePlayers.forEach(function(gamePlayer){

        if (gamePlayer.gpid == getParameterByName('gp')) {

          playerInfo[0] =gamePlayer.player.mail;
  
        } else {

          playerInfo[1] =gamePlayer.player.mail;

        }

      });

      $('#playerInfo').text(playerInfo[0] + '(you) vs ' + (playerInfo[1] == null ? "waiting oponent" : playerInfo[1] ) );

      data.ships.forEach(function (shipPiece) {

        shipPiece.locations.forEach(function (shipLocation) {

          $('#T1' + shipLocation).addClass('ship-piece');
          $('#T1' + shipLocation).addClass(shipPiece.type);

        })

      });

      data.salvos.forEach(function (salvo) {

        if (salvo.gamePlayer != getParameterByName('gp')) {

          salvo.salvoLocations.forEach(function (salvoLocation) {

            if ($('#T1' + salvoLocation).hasClass('ship-piece')) {

              $('#T1' + salvoLocation).addClass('ship-piece-hited');

            } else {

              $('#T1' + salvoLocation).addClass('salvo');

            }

          })

        } else {

          salvo.salvoLocations.forEach(function (salvoLocation) {

            $('#T2' + salvoLocation).addClass('salvo');
            $('#T2' + salvoLocation).html(salvo.turn);

          })

        }

      });

      makeGameRecordTable(data.hits.opponent, "gameRecordOppTable");
      makeGameRecordTable(data.hits.self, "gameRecordSelfTable");

    })
    .fail(function (jqXHR, textStatus) {

      if (jqXHR.status == 401) {

        alert(jqXHR.responseText);

      } else {

        alert("Failed: " + textStatus);

      }


    });
}

document.addEventListener('DOMContentLoaded', function () {

  M.AutoInit();

});

var salvo = {

  locations : []

};

$(".salvoCell").click(function(){

  if ($(this).hasClass('salvo')){

    if(salvo.locations.indexOf(($(this).attr("id")).substr(2)) < 0 ){

      alert("No se puede remover");

    }else{

      $(this).removeClass('salvo');
      salvo.locations.splice(salvo.locations.indexOf(($(this).attr("id")).substr(2)), 1 );

    }

  } else {

    if(salvo.locations.length == 5){

      alert("No se puede hacer mas de 5 tiros");

    }else{

      $(this).addClass('salvo');
      salvo.locations.push(($(this).attr("id")).substr(2));

    }

  }  

});

$("#btnShootSalvo").click(function(){

  if(salvo.locations.length == 0){

    alert("Salvo vacio");

    return null;

  }

  $.ajax({
    
    type: 'POST',
    contentType: 'application/json; charset=utf-8',
    url: "/api/games/players/"+getParameterByName('gp')+"/salvos",

    data: JSON.stringify(salvo),

    success: function (respuesta) {

      //console.log(respuesta);

      //location.href = "game.html?gp=" + getParameterByName('gp');
      location.reload(true);

    },

    error: function (jqXHR) {

      if (jqXHR.status === 0) {

        alert('Not connect: Verify Network.');

      } else {

        alert(jqXHR.responseText);

        console.log(jqXHR);

      }

    }

  });

});

function makeGameRecordTable (hitsArray, gameRecordTableId) {

  var tableId = "#" + gameRecordTableId + " tbody";
  $(tableId).empty();
  var shipsAfloat = 5;
  var playerTag;
  if (gameRecordTableId == "gameRecordOppTable") {
      playerTag = "#opp";
  }
  if (gameRecordTableId == "gameRecordSelfTable") {
      playerTag = "#";
  }

  hitsArray.forEach(function (playTurn) {
      var hitsReport = "";
      if (playTurn.damages.carrierHits > 0){
          hitsReport += "Carrier " + addDamagesIcons(playTurn.damages.carrierHits, "hit") + " ";
          if (playTurn.damages.carrier === 5){
              hitsReport += "SUNK! ";
              shipsAfloat--;
          }
      }

      if (playTurn.damages.battleshipHits > 0){
          hitsReport += "Battleship " + addDamagesIcons(playTurn.damages.battleshipHits, "hit") + " ";
          if (playTurn.damages.battleship === 4){
              hitsReport += "SUNK! ";
              shipsAfloat--;
          }
      }
      if (playTurn.damages.submarineHits > 0){
          hitsReport += "Submarine " + addDamagesIcons(playTurn.damages.submarineHits, "hit") + " ";
          if (playTurn.damages.submarine === 3){
              hitsReport += "SUNK! ";
              shipsAfloat--;
          }
      }
      if (playTurn.damages.destroyerHits > 0){
          hitsReport += "Destroyer " + addDamagesIcons(playTurn.damages.destroyerHits, "hit") + " ";
          if (playTurn.damages.destroyer === 3){
              hitsReport += "SUNK! ";
              shipsAfloat--;
          }
      }
      if (playTurn.damages.patrolboatHits > 0){
          hitsReport += "Patrol Boat " + addDamagesIcons(playTurn.damages.patrolboatHits, "hit") + " ";
          if (playTurn.damages.patrolboat === 2){
              hitsReport += "SUNK! ";
              shipsAfloat--;
          }
      }

      if (playTurn.missed > 0){
          hitsReport +=  "Missed shots " + addDamagesIcons(playTurn.missed, "missed") + " ";
      }

      if (hitsReport === ""){
          hitsReport = "All salvoes missed! No damages!"
      }

      $('<tr><td class="textCenter">' + playTurn.turn + '</td><td>' + hitsReport + '</td></tr>').prependTo(tableId);

  });
  $('#shipsLeftSelfCount').text(shipsAfloat);
}

function addDamagesIcons (numberOfHits, hitOrMissed) {
  var damagesIcons = "";
  if (hitOrMissed === "missed") {
      for (var i = 0; i < numberOfHits; i++) {
          damagesIcons += "<object class='missedShot'></object> "
      }
  }
      if (hitOrMissed === "hit") {
          for (var i = 0; i < numberOfHits; i++) {
              damagesIcons += "<object class='hitShot'></object> "
          }
  }
  return damagesIcons;
}