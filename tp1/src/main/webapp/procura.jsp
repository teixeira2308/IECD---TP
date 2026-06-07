<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    if (session.getAttribute("user") == null) {
        response.sendRedirect("login.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Procurar Oponentes - AutoComplete</title>
    <style>
        .sugestoes-box {
            border: 1px solid #ccc;
            max-width: 200px;
            display: none;
            position: absolute;
            background: white;
        }
        .sugestao-item {
            padding: 5px;
            cursor: pointer;
        }
        .sugestao-item:hover {
            background-color: #f0f0f0;
        }
    </style>
</head>
<body>
    <h2>Procurar Oponentes</h2>
    <a href="lobby.jsp">Voltar ao Lobby</a>
    <br><br>
    
    <label for="campoPesquisa">Introduza o nickname:</label>
    <input type="text" id="campoPesquisa" autocomplete="off" onkeyup="efetuarPesquisa()">
    <div id="caixaSugestoes" class="sugestoes-box"></div>

    <script>
        function efetuarPesquisa() {
            var input = document.getElementById("campoPesquisa");
            var caixa = document.getElementById("caixaSugestoes");
            var termo = input.value.trim();
            
            if (termo.length === 0) {
                caixa.style.display = "none";
                caixa.innerHTML = "";
                return;
            }
            
            var xhr = new XMLHttpRequest();
            xhr.open("GET", "procurarOponentes?termo=" + encodeURIComponent(termo), true);
            
            xhr.onreadystatechange = function() {
                if (xhr.readyState === 4 && xhr.status === 200) {
                    var nicks = xhr.responseText.trim().split("\n");
                    caixa.innerHTML = "";
                    
                    if (nicks.length > 0 && nicks[0] !== "") {
                        caixa.style.display = "block";
                        
                        nicks.forEach(function(nick) {
                            var item = document.createElement("div");
                            item.className = "sugestao-item";
                            item.innerText = nick;
                            
                            item.onclick = function() {
                                input.value = nick;
                                caixa.style.display = "none";
                            };
                            
                            caixa.appendChild(item);
                        });
                    } else {
                        caixa.style.display = "none";
                    }
                }
            };
            xhr.send();
        }
    </script>
</body>
</html>