<!DOCTYPE html>

<%@ page pageEncoding="utf-8" contentType="text/html" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>


<html>
<head>
    <title>Test</title>
</head>
<body>
    
    <h3>Your request succesfully applied!</h3>

    <c:url value="/index" var="indexUrl"/>
    <p><a href="${indexUrl}">To main page</a></p>

</body>
</html>
