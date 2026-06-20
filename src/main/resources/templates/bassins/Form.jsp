<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head><title>Formulaire bassin</title></head>
<body>
    <h1>${modeEdition ? "Modifier" : "Créer"} un bassin</h1>

    <c:if test="${not empty erreur}">
        <p style="color:red">${erreur}</p>
    </c:if>

    <c:choose>
        <c:when test="${modeEdition}">
            <form action="${pageContext.request.contextPath}/bassins/${idBassin}/modifier" method="post">
        </c:when>
        <c:otherwise>
            <form action="${pageContext.request.contextPath}/bassins" method="post">
        </c:otherwise>
    </c:choose>

        <label>Code :</label>
        <input type="text" name="code" value="${bassinDTO.code}" /><br/>

        <label>Surface (m²) :</label>
        <input type="text" name="surface_m2" value="${bassinDTO.surface_m2}" /><br/>

        <label>Profondeur (m) :</label>
        <input type="text" name="profondeur_metre" value="${bassinDTO.profondeur_metre}" /><br/>

        <label>Notes :</label>
        <textarea name="notes">${bassinDTO.notes}</textarea><br/>

        <button type="submit">Enregistrer</button>
    </form>

    <a href="${pageContext.request.contextPath}/bassins">Retour à la liste</a>
</body>
</html>