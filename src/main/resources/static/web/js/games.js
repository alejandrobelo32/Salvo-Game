function loadGames() {
  $.get("/api/games")
    .done(function (data) {
      //renderGamesTable(JSON.stringify(data, null, 2));
      renderGamesTable(data);

      showPlayer(data.player);

    })
    .fail(function (jqXHR, textStatus) {
      showOutput("Failed: " + textStatus);
    });
}

function showPlayer(data) {

  if (data == "guest") {

    $('#player').append("");

    //$('#btnLogout').hide();

    $('#btnCreateGame').hide();

    $('#frmLogin').show();

  } else {

    $('#player').append(data.mail);

    $('#btnLogout').show();

    $('#btnLogin').hide();

    $('#btnSignup').hide();

    $('#btnCreateGame').show();

    $('#frmLogin').hide();

  }

}

function renderGamesTable(data) {

  // document.getElementById('gamesTable');

  // var body = document.getElementsByTagName("body")[0];
  // var tabla   = document.createElement("table");
  // var tblBody = document.createElement("tbody");
 
  // data.games.forEach(function (currentGame) {

  //   var row = document.createElement("tr");

  //   var cellCreated = document.createElement("td");

  //   cellCreated.appendChild(document.createTextNode(currentGame.created));

  //   row.appendChild(cellCreated);

  //   var cellPlayer1 = document.createElement("td");

  //   cellPlayer1.appendChild(document.createTextNode(currentGame.gamePlayers[0].player.mail));

  //   row.appendChild(cellPlayer1);

  //   var cellPlayer2 = document.createElement("td");

  //   cellPlayer2.appendChild(document.createTextNode(currentGame.gamePlayers[1] == null ? "-" : currentGame.gamePlayers[1].player.mail));

  //   row.appendChild(cellPlayer2);

  //   var cellButton = document.createElement("td");
    
  //   var a = document.createElement("a");

  //   a.setAttribute('class', 'waves-effect waves-light btn');
  //   a.setAttribute('href', 'google.com');
  //   a.setAttribute('disabled','true');

  //   var texto = document.createTextNode('You must login to play') ;

  //   a.appendChild(texto);

  //   cellButton.appendChild(a);

  //   row.appendChild(cellButton);
 
  //   // agrega la hilera al final de la tabla (al final del elemento tblbody)
  //   tblBody.appendChild(row);

  // });

  // tabla.appendChild(tblBody);
  // // appends <table> into <body>
  // body.appendChild(tabla);

  let games = data.games.map(function (currentGame) {

    let game = "<tr>";

    game += '<td>' + currentGame.created + '</td>';

    game += '<td> ' + currentGame.gamePlayers[0].player.mail + ' </td>';

    if ((currentGame.gamePlayers[1] == null)) {

      game += '<td>-</td>';

    } else {

      game += '<td> ' + currentGame.gamePlayers[1].player.mail + ' </td>';

    }

    if (data.player == 'guest') {

      game += '<td> <a class="waves-effect waves-light btn" onclick="" disabled>You must login to play</a></td>';

    } else {

      if(currentGame.gamePlayers[0] && currentGame.gamePlayers[1]){

        if (data.player.mail == currentGame.gamePlayers[0].player.mail || data.player.mail == currentGame.gamePlayers[1].player.mail){

          game += '<td> <a class="waves-effect waves-light btn btnEnter" data-gid="'+currentGame.gid+'">Enter the game</a> </td>';

        }else{

          game += '<td> <a class="waves-effect waves-light btn" disabled>GAME IS FULL</a> </td>';

        }

      }else{

        if(data.player.mail == currentGame.gamePlayers[0].player.mail){

          game += '<td> <a class="waves-effect waves-light btn btnEnter" data-gid="'+currentGame.gid+'">Enter the game</a> </td>';
  
        } else {

          game += '<td> <a class="waves-effect waves-light btn " onclick="joinGame(' + currentGame.gid + ')">JOIN</a> </td>';
  
        }

      }

    }

    game += "</tr>";

    return game;

  }).join('');

  $('#games').append(games);

  $(".btnEnter").click(function(){

    //console.log("hola");
  
    var gameID = ($(this).attr("data-gid"));
  
    $.get('/api/game/'+gameID+'/players/')
  
      .done(function (data) {
  
        console.log(data);
  
        location.href = "game.html?gp=" + data.gamePlayers[0].gpid;
  
      })
      
      .fail(function (jqXHR) {
  
        if (jqXHR.status === 0) {
  
          alert('Not connect: Verify Network.');
  
        } else {
  
          alert(jqXHR.responseText);
  
          console.log(jqXHR);
  
        }
  
      });
  
  });

}

loadGames();

function loadLeaderboard() {

  $.get("/api/leaderboard")
    .done(function (data) {

      //renderGamesTable(JSON.stringify(data, null, 2));
      let orderedData = data.sort(function (a, b) {
        return b.scores.total - a.scores.total
      });

      renderLeaderboardTable(orderedData);

    })
    .fail(function (jqXHR, textStatus) {
      showOutput("Failed: " + textStatus);
    });

}

function renderLeaderboardTable(data) {

  let leaderboard = data.map(function (player) {

    let game = "<tr>";

    game += '<td>' + player.userName + '</td>';

    game += '<td>' + player.scores.total + '</td>';

    game += '<td>' + player.scores.won + '</td>';

    game += '<td>' + player.scores.tied + '</td>';

    game += '<td>' + player.scores.lost + '</td>';

    game += "</tr>";

    return game;

  }).join('');

  $('#leaderboard').append(leaderboard);

}

loadLeaderboard();

$("#btnLogin").click(function() {

  $.ajax({
    url: '/api/login',
    data: {
      username: document.getElementById("username").value,
      password: document.getElementById("password").value
    },
    type: 'POST',

    success: function (respuesta) {

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

$("#btnSignup").click(function signup() {

  let username = document.getElementById("username").value;
  let password = document.getElementById("password").value;

  if (password.length < 5) {

    alert("La contraseña debe ser mas larga");
    return null;

  }

  $.ajax({
    url: '/api/players',
    data: {
      username: username,
      password: password
    },
    type: 'POST',

    success: function (respuesta) {

      //alert("Signup succes");

      $("#btnLogin").click();

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

$("#btnLogout").click(function (){

  $.ajax({
    url: '/api/logout',
    type: 'POST',

    success: function (respuesta) {

      //alert("Logged out");

      location.reload(true);

    },

    error: function () {

      console.log("No se ha podido obtener la información");

    }
  });

});


$("#btnCreateGame").click(function(){

  $.ajax({
    url: '/api/games',
    type: 'POST',

    success: function (respuesta) {

      //console.log(respuesta);

      location.href = "game.html?gp=" + respuesta.gpid;

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

function joinGame(gameID) {

  $.ajax({
    url: '/api/game/' + gameID + '/players',
    type: 'POST',

    success: function (respuesta) {

      //console.log(respuesta);

      location.href = "game.html?gp=" + respuesta.gpid;

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

}

$(".enterLogin").keypress(function(e) {
  if(e.which == 13) {

    $("#btnLogin").click();

  }
});

document.addEventListener('DOMContentLoaded', function () {

  M.AutoInit();

});