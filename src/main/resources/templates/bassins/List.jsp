<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head><title>Liste des bassins</title></head>
<body>
    <h1>Liste des bassins</h1>

    <a href="${pageContext.request.contextPath}/bassins/nouveau">+ Nouveau bassin</a>

    <table border="1">
        <tr>
            <th>Code</th>
            <th>Surface (m²)</th>
            <th>Profondeur (m)</th>
            <th>Statut</th>
            <th>Actions</th>
        </tr>
        <c:forEach var="bassin" items="${bassins}">
            <tr>
                <td>${bassin.code}</td>
                <td>${bassin.surfaceM2}</td>
                <td>${bassin.profondeurMetre}</td>
                <td>${bassin.statutActuel.libelle}</td>
                <td>
                    <a href="${pageContext.request.contextPath}/bassins/${bassin.id}/modifier">Modifier</a>
                    <form action="${pageContext.request.contextPath}/bassins/${bassin.id}/supprimer"
                          method="post" style="display:inline">
                        <button type="submit">Supprimer</button>
                    </form>
                </td>
            </tr>
        </c:forEach>
    </table>
</body>
</html>