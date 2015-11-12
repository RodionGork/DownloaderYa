<!DOCTYPE html>

<%@ page pageEncoding="utf-8" contentType="text/html" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>


<html>
<head>
    <title>Download Controller</title>
</head>
<body>
    
    <h3>State of your downloads</h3>
    
    <p>Active:</p>
    <ul>
        <c:forEach items="${active}" var="item">
            <li><c:out value="${item.key} - ${item.value}"/>
                <c:url value="/stop" var="stopLink">
                    <c:param name="link" value="${item.key}"/>
                </c:url>
                <a href="${stopLink}">stop</a>
            </li>
        </c:forEach>
    </ul>
    
    <br/>
    
    <p>Finished:</p>
    <ul>
        <c:forEach items="${finished}" var="item">
            <li><c:out value="${item.key} - ${item.value}"/>
            </li>
        </c:forEach>
    </ul>
    
    <c:url value="/index" var="indexUrl"/>
    <p><a href="${indexUrl}">To main page</a></p>
    
    <script>
        setTimeout(function() {location.href = location.href;}, 1000);
    </script>
</body>
</html>
