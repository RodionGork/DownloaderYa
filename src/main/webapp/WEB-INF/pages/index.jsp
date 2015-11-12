<!DOCTYPE html>

<%@ page pageEncoding="utf-8" contentType="text/html" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>


<html>
<head>
    <title>Download Manager</title>
</head>
<body>
    
    <h3>Add resource for downloading</h3>

    <form action="download">
        <input type="text" name="link"/>
        <input type="submit"/>
    </form>

    <c:url value="/state" var="stateUrl"/>
    <p><a href="${stateUrl}">View download state</a></p>

</body>
</html>
