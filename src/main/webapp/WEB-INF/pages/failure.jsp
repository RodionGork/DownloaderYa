<!DOCTYPE html>

<%@ page pageEncoding="utf-8" contentType="text/html" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>


<html>
<head>
    <title>Failure</title>
</head>
<body>
    
    <h3>Error occurs:</h3>

    <p><c:out value="${message}"/></p>
    
    <c:url value="/index" var="indexUrl"/>
    <p><a href="${indexUrl}">To main page</a></p>

</body>
</html>
